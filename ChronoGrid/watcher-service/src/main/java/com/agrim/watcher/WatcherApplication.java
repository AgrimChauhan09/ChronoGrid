package com.agrim.watcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Watcher Service — polls MongoDB for stuck/timed-out jobs and retries them.
 * Runs on port 8083 by default.
 */
@SpringBootApplication
@EnableScheduling
public class WatcherApplication {
    public static void main(String[] args) {
        SpringApplication.run(WatcherApplication.class, args);
    }
}
