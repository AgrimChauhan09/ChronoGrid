package com.agrim.watcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Watcher Service — polls MongoDB for stuck/timed-out jobs and retries them.
 * Uses periodic @Scheduled polling (maps to pkg/watcher/watcher.go).
 * Maps to cmd/watcher/main.go in the Go project.
 * Runs on port 8083 by default.
 */
@SpringBootApplication
@EnableScheduling
public class WatcherApplication {
    public static void main(String[] args) {
        SpringApplication.run(WatcherApplication.class, args);
    }
}
