package com.agrim.coordinator.model;

import lombok.Builder;
import lombok.Data;

/**
 * CoordinatorStats — metrics for the coordinator.
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
