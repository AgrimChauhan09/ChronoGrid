package com.agrim.common.queue;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QueueStats {

    private int currentSize;

    private int capacity;

    private int remainingCapacity;

    private long totalEnqueued;

    private long totalDequeued;

    private long totalDropped;
}
