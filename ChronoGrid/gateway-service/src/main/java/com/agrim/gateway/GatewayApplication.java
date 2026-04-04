package com.agrim.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Gateway Service — REST API entry point.
 * Routes all incoming client requests to the appropriate internal service.
 * Maps to cmd/gateway/main.go in the Go project.
 * Runs on port 8080 by default.
 */
@SpringBootApplication
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
