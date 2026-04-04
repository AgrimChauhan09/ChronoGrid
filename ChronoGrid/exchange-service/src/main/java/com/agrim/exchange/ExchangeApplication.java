package com.agrim.exchange;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Exchange Service — internal message broker without Apache Kafka.
 * Receives events from services and routes them to the correct destination.
 * Uses in-memory maps + REST callbacks instead of a message bus.
 * Maps to cmd/exchange/main.go and pkg/exchange/exchange.go in the Go project.
 * Runs on port 8084 by default.
 */
@SpringBootApplication
public class ExchangeApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExchangeApplication.class, args);
    }
}
