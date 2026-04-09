package com.agrim.coordinator.service;

import com.agrim.common.loadbalancer.LoadBalancer;
import com.agrim.common.model.Job;
import com.agrim.common.model.WorkerInfo;
import com.agrim.coordinator.model.CoordinatorDocument;
import com.agrim.coordinator.model.CoordinatorStats;
import com.agrim.coordinator.repository.CoordinatorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/*
 * CoordinatorService — polls the queue and dispatches jobs to available workers.
 * Uses LoadBalancer for worker selection.
 */
@Service
public class CoordinatorService {

    private static final Logger log = LoggerFactory.getLogger(CoordinatorService.class);

    private final LoadBalancer loadBalancer;
    private final CoordinatorRepository coordinatorRepository;
    private final RestTemplate restTemplate;

    private static final String QUEUE_DEQUEUE_URL = "http://localhost:8085/queue/dequeue";

    // FIX ADDED: Re-queue karne ke liye naya URL add kiya hai (Queue Service ka)
    private static final String QUEUE_ENQUEUE_URL = "http://localhost:8085/queue/enqueue";

    private static final String WORKER_EXECUTE_URL = "http://%s:%d/workers/execute";

    private final AtomicLong totalDispatched = new AtomicLong(0);
    private final AtomicLong totalFailed = new AtomicLong(0);

    public CoordinatorService(LoadBalancer loadBalancer,
                              CoordinatorRepository coordinatorRepository,
                              RestTemplate restTemplate) {
        this.loadBalancer = loadBalancer;
        this.coordinatorRepository = coordinatorRepository;
        this.restTemplate = restTemplate;
    }

    @Scheduled(fixedDelay = 1000)
    public void coordinate() {
        if (loadBalancer.workerCount() == 0) {
            log.info("Polling queue... but waiting for workers to register.");
            return;
        }

        try {
            Job job = fetchFromQueue();
            if (job == null) return;

            Optional<WorkerInfo> worker = loadBalancer.selectLeastLoaded();
            if (worker.isEmpty()) {
                log.warn("No available worker — re-queuing job {}", job.getId());

                // FIX ADDED: Job hawa mein gayab na ho, isliye usko wapas queue mein push kar rahe hain
                requeueJob(job);

                return;
            }

            dispatch(job, worker.get());
        } catch (Exception e) {
            log.error("Coordinator error: {}", e.getMessage());
        }
    }

    private Job fetchFromQueue() {
        try {
            return restTemplate.getForObject(QUEUE_DEQUEUE_URL, Job.class);
        } catch (Exception e) {
            // FIX ADDED: Error ko silently ignore karne ki bajay ab hum exact error reason print karenge
            log.error("Failed to fetch from queue (Is Queue Service running?): {}", e.getMessage());
            return null;
        }
    }

    private void dispatch(Job job, WorkerInfo worker) {
        String workerUrl = String.format(WORKER_EXECUTE_URL, worker.getHost(), worker.getPort());

        try {
            restTemplate.postForEntity(workerUrl, job, String.class);
            totalDispatched.incrementAndGet();

            CoordinatorDocument record = CoordinatorDocument.builder()
                    .jobId(job.getId())
                    .workerId(worker.getWorkerId())
                    .workerUrl(workerUrl)
                    .result("DISPATCHED")
                    .dispatchedAt(Instant.now())
                    .build();
            coordinatorRepository.save(record);

            log.info("Dispatched job {} to worker {}", job.getId(), worker.getWorkerId());
        } catch (Exception e) {
            log.error("Failed to dispatch job {} to worker {}: {}", job.getId(), worker.getWorkerId(), e.getMessage());
            totalFailed.incrementAndGet();
        }
    }

    // FIX ADDED: Ye naya method hai jo Job ko queue mein wapas daalne ka kaam karega
    private void requeueJob(Job job) {
        try {
            restTemplate.postForObject(QUEUE_ENQUEUE_URL, job, Void.class);
            log.info("Successfully re-queued job {}", job.getId());
        } catch (Exception e) {
            log.error("CRITICAL: Failed to re-queue job {}. Error: {}", job.getId(), e.getMessage());
        }
    }

    public void registerWorker(WorkerInfo worker) {
        loadBalancer.registerWorker(worker);
        log.info("Worker registered with coordinator: {}", worker.getWorkerId());
    }

    public void deregisterWorker(String workerId) {
        loadBalancer.deregisterWorker(workerId);
    }

    public CoordinatorStats getStats() {
        return CoordinatorStats.builder()
                .totalDispatched(totalDispatched.get())
                .totalSucceeded(totalDispatched.get() - totalFailed.get())
                .totalFailed(totalFailed.get())
                .activeWorkers(loadBalancer.workerCount())
                .build();
    }
}