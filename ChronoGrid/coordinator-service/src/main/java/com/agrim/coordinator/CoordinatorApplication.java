package com.agrim.coordinator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Coordinator Service — worker-queue coordinator.
 * Polls the queue, selects an available worker via LoadBalancer,
 * and dispatches the job to the worker via REST.
 * Maps to cmd/coordinator/main.go and pkg/worker-queue-coordinator/ in the Go project.
 * Runs on port 8086 by default.
 */
@SpringBootApplication
@EnableScheduling
public class CoordinatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(CoordinatorApplication.class, args);
    }
}
