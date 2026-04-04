package com.agrim.jobscheduler.controller;

import com.agrim.common.dto.JobRequest;
import com.agrim.common.dto.JobResponse;
import com.agrim.jobscheduler.service.JobSchedulerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * JobController — REST API for submitting and monitoring jobs.
 * This is the primary client-facing API of the ChronoGrid system.
 * Maps to cmd/job_scheduler/main.go HTTP handlers.
 */
@RestController
@RequestMapping("/jobs")
public class JobController {

    private final JobSchedulerService jobSchedulerService;

    public JobController(JobSchedulerService jobSchedulerService) {
        this.jobSchedulerService = jobSchedulerService;
    }

    @PostMapping("/submit")
    public ResponseEntity<JobResponse> submit(@RequestBody JobRequest request) {
        return ResponseEntity.ok(jobSchedulerService.submitJob(request));
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<JobResponse> getJob(@PathVariable String jobId) {
        JobResponse job = jobSchedulerService.getJob(jobId);
        if (job == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(job);
    }

    @GetMapping
    public ResponseEntity<List<JobResponse>> getAllJobs() {
        return ResponseEntity.ok(jobSchedulerService.getAllJobs());
    }

    @GetMapping("/running")
    public ResponseEntity<List<JobResponse>> getRunning() {
        return ResponseEntity.ok(jobSchedulerService.getRunningJobs());
    }

    @PutMapping("/{jobId}/status")
    public ResponseEntity<String> updateStatus(@PathVariable String jobId,
                                               @RequestBody Map<String, String> body) {
        jobSchedulerService.updateStatus(jobId, body.get("status"));
        return ResponseEntity.ok("Status updated");
    }

    @PostMapping("/{jobId}/retry")
    public ResponseEntity<String> retry(@PathVariable String jobId) {
        jobSchedulerService.retryJob(jobId);
        return ResponseEntity.ok("Retry triggered for: " + jobId);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> stats() {
        return ResponseEntity.ok(jobSchedulerService.getStatusCounts());
    }
}
