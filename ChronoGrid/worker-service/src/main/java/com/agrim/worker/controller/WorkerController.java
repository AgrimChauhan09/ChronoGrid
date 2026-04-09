package com.agrim.worker.controller;

import com.agrim.common.model.Job;
import com.agrim.worker.model.WorkerStats;
import com.agrim.worker.service.WorkerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * WorkerController — exposes endpoints for coordinator to dispatch jobs to this worker.
 */
@RestController
@RequestMapping("/workers")
public class WorkerController {

    private final WorkerService workerService;

    public WorkerController(WorkerService workerService) {
        this.workerService = workerService;
    }

    @PostMapping("/execute")
    public ResponseEntity<String> executeJob(@RequestBody Job job) {
        workerService.executeJob(job);
        return ResponseEntity.accepted().body("Job accepted: " + job.getId());
    }

    @GetMapping("/stats")
    public ResponseEntity<WorkerStats> getStats() {
        return ResponseEntity.ok(workerService.getStats());
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
