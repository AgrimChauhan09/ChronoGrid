package com.agrim.watcher.service;

import com.agrim.watcher.model.WatcherDocument;
import com.agrim.watcher.repository.WatcherRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * WatcherService — polls MongoDB for stuck or timed-out jobs and retries them.
 * Maps to pkg/watcher/watcher.go and helpers.go in the Go project.
 */
@Service
public class WatcherService {

    private static final Logger log = LoggerFactory.getLogger(WatcherService.class);

    private final WatcherRepository watcherRepository;
    private final RestTemplate restTemplate;

    private static final String JOB_SCHEDULER_RUNNING_URL = "http://localhost:8087/jobs/running";
    private static final String JOB_RETRY_URL = "http://localhost:8087/jobs/%s/retry";
    private static final long TIMEOUT_THRESHOLD_SECONDS = 300;

    public WatcherService(WatcherRepository watcherRepository, RestTemplate restTemplate) {
        this.watcherRepository = watcherRepository;
        this.restTemplate = restTemplate;
    }

    @Scheduled(fixedDelay = 30000)
    public void watchRunningJobs() {
        log.debug("Watcher scanning for stuck jobs...");

        try {
            List<Map> runningJobs = fetchRunningJobs();

            for (Map<?, ?> job : runningJobs) {
                String jobId = (String) job.get("id");
                String startedAtStr = (String) job.get("startedAt");

                if (startedAtStr == null) continue;

                Instant startedAt = Instant.parse(startedAtStr);
                long runningSeconds = Instant.now().getEpochSecond() - startedAt.getEpochSecond();

                if (runningSeconds > TIMEOUT_THRESHOLD_SECONDS) {
                    log.warn("Job {} has been running for {}s — marking as timed out", jobId, runningSeconds);
                    handleTimeout(jobId, runningSeconds);
                }
            }
        } catch (Exception e) {
            log.error("Watcher scan failed: {}", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map> fetchRunningJobs() {
        try {
            return restTemplate.getForObject(JOB_SCHEDULER_RUNNING_URL, List.class);
        } catch (Exception e) {
            log.warn("Could not fetch running jobs: {}", e.getMessage());
            return List.of();
        }
    }

    private void handleTimeout(String jobId, long runningSeconds) {
        try {
            String retryUrl = String.format(JOB_RETRY_URL, jobId);
            restTemplate.postForEntity(retryUrl, null, String.class);

            WatcherDocument event = WatcherDocument.builder()
                    .jobId(jobId)
                    .action("RETRY")
                    .reason("Job timed out after " + runningSeconds + " seconds")
                    .detectedAt(Instant.now())
                    .build();
            watcherRepository.save(event);
        } catch (Exception e) {
            log.error("Failed to retry job {}: {}", jobId, e.getMessage());
        }
    }

    public List<WatcherDocument> getEventsForJob(String jobId) {
        return watcherRepository.findByJobId(jobId);
    }
}
