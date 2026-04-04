package com.agrim.exchange.model;

import lombok.Builder;
import lombok.Data;

/**
 * ExchangeStats — metrics for the exchange service.
 * Maps to pkg/exchange/exchange_stats.go in the Go project.
 */
@Data
@Builder
public class ExchangeStats {

    private long totalMessagesReceived;

    private long totalMessagesRouted;

    private long totalMessagesFailed;

    private int subscriberCount;
}
