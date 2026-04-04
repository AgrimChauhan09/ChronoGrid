package com.agrim.queueservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Queue Service — manages the job queue (enqueue / dequeue / stats).
 * Backed by a LinkedBlockingQueue in-memory with MongoDB persistence.
 * No Apache Kafka — pure Java queue.
 * Maps to cmd/queue_service/main.go and pkg/queue/ in the Go project.
 * Runs on port 8085 by default.
 */
@SpringBootApplication
public class QueueServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(QueueServiceApplication.class, args);
    }
}
