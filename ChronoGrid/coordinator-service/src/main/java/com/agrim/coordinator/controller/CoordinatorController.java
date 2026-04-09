package com.agrim.coordinator.controller;

import com.agrim.common.model.WorkerInfo;
import com.agrim.coordinator.model.CoordinatorStats;
import com.agrim.coordinator.service.CoordinatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/*
 * CoordinatorController — manages worker registration and exposes metrics.
 */
@RestController
@RequestMapping("/coordinator")
public class CoordinatorController {

    private final CoordinatorService coordinatorService;

    public CoordinatorController(CoordinatorService coordinatorService) {
        this.coordinatorService = coordinatorService;
    }

    @PostMapping("/workers/register")
    public ResponseEntity<String> registerWorker(@RequestBody WorkerInfo worker) {
        coordinatorService.registerWorker(worker);
        return ResponseEntity.ok("Worker registered: " + worker.getWorkerId());
    }

    @DeleteMapping("/workers/{workerId}")
    public ResponseEntity<String> deregisterWorker(@PathVariable String workerId) {
        coordinatorService.deregisterWorker(workerId);
        return ResponseEntity.ok("Worker removed: " + workerId);
    }

    @GetMapping("/stats")
    public ResponseEntity<CoordinatorStats> stats() {
        return ResponseEntity.ok(coordinatorService.getStats());
    }
}
