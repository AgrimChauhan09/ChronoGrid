package com.agrim.scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Scheduler Service — triggers cron-based jobs on a schedule.
 * Uses Spring's @Scheduled (maps to pkg/scheduler/cron.go and scheduler.go).
 * Includes leader election so only one instance fires at a time.
 * Maps to cmd/scheduler/main.go in the Go project.
 * Runs on port 8081 by default.
 */
@SpringBootApplication
@EnableScheduling
public class SchedulerApplication {
    public static void main(String[] args) {
        SpringApplication.run(SchedulerApplication.class, args);
    }
}
