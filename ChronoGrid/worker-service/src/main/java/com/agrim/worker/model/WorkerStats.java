package com.agrim.worker.model;

import lombok.Builder;
import lombok.Data;

/**
 * WorkerStats — runtime metrics for a worker.
 * Maps to pkg/worker/worker_stats.go in the Go project.
 */
@Data
@Builder
public class WorkerStats {

    private String workerId;

    private int activeJobs;

    private int maxConcurrentJobs;

    private long totalExecuted;

    private long totalSucceeded;

    private long totalFailed;

    private double successRate;
}
