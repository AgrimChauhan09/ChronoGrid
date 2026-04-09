package com.agrim.exchange.service;

import com.agrim.exchange.model.ExchangeMessage;
import com.agrim.exchange.model.ExchangeStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ExchangeService — routes events between services without Apache Kafka.
 * Subscribers register their callback URL; exchange POSTs the message to them.
 */
@Service
public class ExchangeService {

    private static final Logger log = LoggerFactory.getLogger(ExchangeService.class);

    private final RestTemplate restTemplate;

    private final Map<String, String> subscribers = new ConcurrentHashMap<>();
    private final AtomicLong totalReceived = new AtomicLong(0);
    private final AtomicLong totalRouted = new AtomicLong(0);
    private final AtomicLong totalFailed = new AtomicLong(0);

    public ExchangeService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void subscribe(String eventType, String callbackUrl) {
        subscribers.put(eventType, callbackUrl);
        log.info("Subscriber registered for event '{}' -> {}", eventType, callbackUrl);
    }

    public void publish(ExchangeMessage message) {
        message.setMessageId(UUID.randomUUID().toString());
        message.setCreatedAt(Instant.now());
        totalReceived.incrementAndGet();

        String callbackUrl = subscribers.get(message.getEventType());
        if (callbackUrl == null) {
            log.warn("No subscriber for event type: {}", message.getEventType());
            totalFailed.incrementAndGet();
            return;
        }

        try {
            restTemplate.postForEntity(callbackUrl, message, String.class);
            totalRouted.incrementAndGet();
            log.debug("Routed event {} to {}", message.getEventType(), callbackUrl);
        } catch (Exception e) {
            log.error("Failed to route event {}: {}", message.getEventType(), e.getMessage());
            totalFailed.incrementAndGet();
        }
    }

    public ExchangeStats getStats() {
        return ExchangeStats.builder()
                .totalMessagesReceived(totalReceived.get())
                .totalMessagesRouted(totalRouted.get())
                .totalMessagesFailed(totalFailed.get())
                .subscriberCount(subscribers.size())
                .build();
    }
}
