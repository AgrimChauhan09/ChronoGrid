package com.agrim.coordinator.model;

import lombok.Builder;
import lombok.Data;

/**
 * CoordinatorStats — metrics for the coordinator.
 * Maps to pkg/worker-queue-coordinator/coordinator_stats.go in the Go project.
 */
@Data
@Builder
public class CoordinatorStats {

    private long totalDispatched;

    private long totalSucceeded;

    private long totalFailed;

    private int activeWorkers;

    private int queueSize;
}
