package com.agrim.exchange.model;

import lombok.Builder;
import lombok.Data;

/**
 * ExchangeStats — metrics for the exchange service.
 */
@Data
@Builder
public class ExchangeStats {

    private long totalMessagesReceived;

    private long totalMessagesRouted;

    private long totalMessagesFailed;

    private int subscriberCount;
}
