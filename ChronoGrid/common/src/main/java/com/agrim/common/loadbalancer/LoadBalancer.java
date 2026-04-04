package com.agrim.common.loadbalancer;

import com.agrim.common.model.WorkerInfo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * LoadBalancer — selects the best available worker for a job.
 * Supports round-robin and least-connections strategies.
 * Maps to pkg/loadbalancer in the Go project.
 */
@Component
public class LoadBalancer {

    private final Map<String, WorkerInfo> workerPool = new ConcurrentHashMap<>();
    private final AtomicInteger roundRobinCounter = new AtomicInteger(0);

    public void registerWorker(WorkerInfo worker) {
        workerPool.put(worker.getWorkerId(), worker);
    }

    public void deregisterWorker(String workerId) {
        workerPool.remove(workerId);
    }

    public void updateWorker(WorkerInfo worker) {
        workerPool.put(worker.getWorkerId(), worker);
    }

    public Optional<WorkerInfo> selectWorker() {
        List<WorkerInfo> available = workerPool.values().stream()
                .filter(w -> w.getStatus() == WorkerInfo.WorkerStatus.IDLE
                        || w.getActiveJobs() < w.getMaxConcurrentJobs())
                .toList();

        if (available.isEmpty()) {
            return Optional.empty();
        }

        int index = Math.abs(roundRobinCounter.getAndIncrement() % available.size());
        return Optional.of(available.get(index));
    }

    public Optional<WorkerInfo> selectLeastLoaded() {
        return workerPool.values().stream()
                .filter(w -> w.getStatus() != WorkerInfo.WorkerStatus.OFFLINE)
                .min((a, b) -> Integer.compare(a.getActiveJobs(), b.getActiveJobs()));
    }

    public int workerCount() {
        return workerPool.size();
    }

    public Map<String, WorkerInfo> getAllWorkers() {
        return Map.copyOf(workerPool);
    }
}
