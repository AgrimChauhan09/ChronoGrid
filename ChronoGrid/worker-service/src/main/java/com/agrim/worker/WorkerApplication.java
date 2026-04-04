package com.agrim.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Worker Service — picks up assigned jobs and executes them.
 * @EnableAsync allows concurrent job execution on a thread pool.
 * Maps to cmd/worker/main.go and pkg/worker/worker.go in the Go project.
 * Runs on port 8082 by default.
 */
@SpringBootApplication
@EnableAsync
public class WorkerApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorkerApplication.class, args);
    }
}
