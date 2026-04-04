package com.agrim.exchange.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * ExchangeMessage — an event routed through the exchange service.
 * Maps to pkg/exchange/structs.go in the Go project.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeMessage {

    private String messageId;

    private String eventType;

    private String sourceService;

    private String targetService;

    private Map<String, Object> payload;

    private Instant createdAt;
}
