package com.agrim.queueservice.controller;

import com.agrim.common.model.Job;
import com.agrim.common.queue.QueueStats;
import com.agrim.queueservice.service.QueueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * QueueController — REST endpoints for queue operations.
 * Maps to cmd/queue_service/main.go HTTP handlers.
 */
@RestController
@RequestMapping("/queue")
public class QueueController {

    private final QueueService queueService;

    public QueueController(QueueService queueService) {
        this.queueService = queueService;
    }

    @PostMapping("/enqueue")
    public ResponseEntity<String> enqueue(@RequestBody Job job) {
        boolean accepted = queueService.enqueue(job);
        if (accepted) {
            return ResponseEntity.ok("Enqueued: " + job.getId());
        }
        return ResponseEntity.status(503).body("Queue is full");
    }

    @GetMapping("/dequeue")
    public ResponseEntity<Job> dequeue() throws InterruptedException {
        Job job = queueService.dequeue();
        if (job == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(job);
    }

    @GetMapping("/stats")
    public ResponseEntity<QueueStats> stats() {
        return ResponseEntity.ok(queueService.getStats());
    }
}
