package com.agrim.jobscheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Job Scheduler Service — main orchestrator of the ChronoGrid system.
 * Accepts job submissions from clients, persists jobs in MongoDB,
 * and hands them off to the Exchange → Queue → Coordinator → Worker pipeline.
 * Maps to cmd/job_scheduler/main.go in the Go project.
 * Runs on port 8087 by default.
 */
@SpringBootApplication
public class JobSchedulerApplication {
    public static void main(String[] args) {
        SpringApplication.run(JobSchedulerApplication.class, args);
    }
}
