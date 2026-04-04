package com.agrim.worker.service;

import com.agrim.common.model.Job;
import com.agrim.common.model.JobStatus;
import com.agrim.worker.model.WorkerDocument;
import com.agrim.worker.model.WorkerStats;
import com.agrim.worker.repository.WorkerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * WorkerService — executes jobs asynchronously on a thread pool.
 * Reports status back to the job-scheduler service.
 * Maps to pkg/worker/worker.go and helpers.go in the Go project.
 */
@Service
public class WorkerService {

    private static final Logger log = LoggerFactory.getLogger(WorkerService.class);

    private final WorkerRepository workerRepository;
    private final RestTemplate restTemplate;

    private final Map<String, Job> activeJobs = new ConcurrentHashMap<>();
    private final AtomicLong totalExecuted = new AtomicLong(0);
    private final AtomicLong totalFailed = new AtomicLong(0);

    @Value("${worker.max-concurrent-jobs:5}")
    private int maxConcurrentJobs;

    @Value("${worker.instance-id:#{T(java.util.UUID).randomUUID().toString()}}")
    private String workerId;

    private static final String JOB_STATUS_URL = "http://localhost:8087/jobs/%s/status";

    public WorkerService(WorkerRepository workerRepository, RestTemplate restTemplate) {
        this.workerRepository = workerRepository;
        this.restTemplate = restTemplate;
    }

    @Async
    public void executeJob(Job job) {
        if (activeJobs.size() >= maxConcurrentJobs) {
            log.warn("Worker at capacity — rejecting job {}", job.getId());
            return;
        }

        activeJobs.put(job.getId(), job);
        log.info("Worker {} executing job: {}", workerId, job.getId());

        try {
            reportStatus(job.getId(), JobStatus.RUNNING);
            performWork(job);
            reportStatus(job.getId(), JobStatus.SUCCESS);
            totalExecuted.incrementAndGet();
            log.info("Job {} completed successfully", job.getId());
        } catch (Exception e) {
            log.error("Job {} failed: {}", job.getId(), e.getMessage());
            reportStatus(job.getId(), JobStatus.FAILED);
            totalFailed.incrementAndGet();
        } finally {
            activeJobs.remove(job.getId());
        }
    }

    private void performWork(Job job) throws InterruptedException {
        log.info("Executing job type: {} with payload: {}", job.getType(), job.getPayload());
        Thread.sleep(500);
    }

    private void reportStatus(String jobId, JobStatus status) {
        try {
            String url = String.format(JOB_STATUS_URL, jobId);
            restTemplate.put(url, Map.of("status", status.name()));
        } catch (Exception e) {
            log.warn("Failed to report status for job {}: {}", jobId, e.getMessage());
        }
    }

    public WorkerStats getStats() {
        long executed = totalExecuted.get();
        long failed = totalFailed.get();
        return WorkerStats.builder()
                .workerId(workerId)
                .activeJobs(activeJobs.size())
                .maxConcurrentJobs(maxConcurrentJobs)
                .totalExecuted(executed)
                .totalSucceeded(executed - failed)
                .totalFailed(failed)
                .successRate(executed > 0 ? (double)(executed - failed) / executed * 100 : 0)
                .build();
    }

    public void registerSelf() {
        WorkerDocument doc = WorkerDocument.builder()
                .workerId(workerId)
                .host("localhost")
                .status("IDLE")
                .maxConcurrentJobs(maxConcurrentJobs)
                .registeredAt(Instant.now())
                .lastHeartbeatAt(Instant.now())
                .build();
        workerRepository.save(doc);
        log.info("Worker {} registered", workerId);
    }
}
