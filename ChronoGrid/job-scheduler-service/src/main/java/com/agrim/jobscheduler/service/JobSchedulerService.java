package com.agrim.jobscheduler.service;

import com.agrim.common.dto.JobRequest;
import com.agrim.common.dto.JobResponse;
import com.agrim.common.model.JobStatus;
import com.agrim.jobscheduler.model.JobDocument;
import com.agrim.jobscheduler.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JobSchedulerService — orchestrates job lifecycle from submission to completion.
 * Persists jobs to MongoDB, forwards them to the queue service.
 * Maps to cmd/job_scheduler/main.go in the Go project.
 */
@Service
public class JobSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(JobSchedulerService.class);

    private final JobRepository jobRepository;
    private final RestTemplate restTemplate;

    private static final String QUEUE_ENQUEUE_URL = "http://localhost:8085/queue/enqueue";

    public JobSchedulerService(JobRepository jobRepository, RestTemplate restTemplate) {
        this.jobRepository = jobRepository;
        this.restTemplate = restTemplate;
    }

    public JobResponse submitJob(JobRequest request) {
        JobDocument job = JobDocument.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .type(request.getType())
                .cronExpression(request.getCronExpression())
                .payload(request.getPayload())
                .maxRetries(request.getMaxRetries() > 0 ? request.getMaxRetries() : 3)
                .timeoutSeconds(request.getTimeoutSeconds() > 0 ? request.getTimeoutSeconds() : 300)
                .status(JobStatus.PENDING)
                .retryCount(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        jobRepository.save(job);
        log.info("Job {} created: {}", job.getId(), job.getName());

        enqueueJob(job);

        return toResponse(job);
    }

    private void enqueueJob(JobDocument job) {
        try {
            job.setStatus(JobStatus.QUEUED);
            job.setUpdatedAt(Instant.now());
            jobRepository.save(job);
            restTemplate.postForEntity(QUEUE_ENQUEUE_URL, job, String.class);
            log.info("Job {} sent to queue", job.getId());
        } catch (Exception e) {
            log.error("Failed to enqueue job {}: {}", job.getId(), e.getMessage());
            job.setStatus(JobStatus.FAILED);
            job.setErrorMessage("Failed to enqueue: " + e.getMessage());
            job.setUpdatedAt(Instant.now());
            jobRepository.save(job);
        }
    }

    public void updateStatus(String jobId, String status) {
        jobRepository.findById(jobId).ifPresent(job -> {
            job.setStatus(JobStatus.valueOf(status));
            job.setUpdatedAt(Instant.now());
            if (JobStatus.RUNNING.name().equals(status)) {
                job.setStartedAt(Instant.now());
            }
            if (JobStatus.SUCCESS.name().equals(status) || JobStatus.FAILED.name().equals(status)) {
                job.setCompletedAt(Instant.now());
            }
            jobRepository.save(job);
        });
    }

    public void retryJob(String jobId) {
        jobRepository.findById(jobId).ifPresent(job -> {
            if (job.getRetryCount() >= job.getMaxRetries()) {
                log.warn("Job {} exceeded max retries ({})", jobId, job.getMaxRetries());
                return;
            }
            job.setRetryCount(job.getRetryCount() + 1);
            job.setStatus(JobStatus.RETRYING);
            job.setUpdatedAt(Instant.now());
            jobRepository.save(job);
            enqueueJob(job);
            log.info("Retrying job {} (attempt {})", jobId, job.getRetryCount());
        });
    }

    public List<JobResponse> getRunningJobs() {
        return jobRepository.findByStatus(JobStatus.RUNNING)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<JobResponse> getAllJobs() {
        return jobRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public JobResponse getJob(String jobId) {
        return jobRepository.findById(jobId).map(this::toResponse).orElse(null);
    }

    public Map<String, Long> getStatusCounts() {
        return Map.of(
            "PENDING", jobRepository.countByStatus(JobStatus.PENDING),
            "QUEUED", jobRepository.countByStatus(JobStatus.QUEUED),
            "RUNNING", jobRepository.countByStatus(JobStatus.RUNNING),
            "SUCCESS", jobRepository.countByStatus(JobStatus.SUCCESS),
            "FAILED", jobRepository.countByStatus(JobStatus.FAILED)
        );
    }

    private JobResponse toResponse(JobDocument doc) {
        return JobResponse.builder()
                .id(doc.getId())
                .name(doc.getName())
                .type(doc.getType())
                .status(doc.getStatus())
                .assignedWorkerId(doc.getAssignedWorkerId())
                .retryCount(doc.getRetryCount())
                .createdAt(doc.getCreatedAt())
                .scheduledAt(doc.getScheduledAt())
                .startedAt(doc.getStartedAt())
                .completedAt(doc.getCompletedAt())
                .errorMessage(doc.getErrorMessage())
                .build();
    }
}
