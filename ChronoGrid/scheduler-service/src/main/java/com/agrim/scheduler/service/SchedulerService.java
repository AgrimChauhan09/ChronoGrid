package com.agrim.scheduler.service;

import com.agrim.common.dto.JobRequest;
import com.agrim.scheduler.model.ScheduleDocument;
import com.agrim.scheduler.repository.ScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;

/**
 * SchedulerService — loads active cron schedules and fires them.
 * Only fires if this instance is the elected leader.
 */
@Service
public class SchedulerService {

    private static final Logger log = LoggerFactory.getLogger(SchedulerService.class);

    private final ScheduleRepository scheduleRepository;
    private final LeaderElectionService leaderElectionService;
    private final RestTemplate restTemplate;

    private static final String JOB_SCHEDULER_URL = "http://localhost:8087/jobs/submit";

    public SchedulerService(ScheduleRepository scheduleRepository,
                            LeaderElectionService leaderElectionService,
                            RestTemplate restTemplate) {
        this.scheduleRepository = scheduleRepository;
        this.leaderElectionService = leaderElectionService;
        this.restTemplate = restTemplate;
    }

    @Scheduled(fixedDelay = 60000)
    public void triggerDueSchedules() {
        if (!leaderElectionService.isLeader()) {
            log.debug("Not the leader — skipping cron tick");
            return;
        }

        List<ScheduleDocument> activeSchedules = scheduleRepository.findByActiveTrue();
        Instant now = Instant.now();

        for (ScheduleDocument schedule : activeSchedules) {
            if (isDue(schedule, now)) {
                fireJob(schedule);
                schedule.setLastTriggeredAt(now);
                scheduleRepository.save(schedule);
            }
        }

        leaderElectionService.renewLease();
    }

    private boolean isDue(ScheduleDocument schedule, Instant now) {
        if (schedule.getNextTriggerAt() == null) return true;
        return now.isAfter(schedule.getNextTriggerAt());
    }

    private void fireJob(ScheduleDocument schedule) {
        try {
            JobRequest request = JobRequest.builder()
                    .name(schedule.getJobName())
                    .type(schedule.getJobType())
                    .cronExpression(schedule.getCronExpression())
                    .build();
            restTemplate.postForEntity(JOB_SCHEDULER_URL, request, String.class);
            log.info("Fired scheduled job: {}", schedule.getJobName());
        } catch (Exception e) {
            log.error("Failed to fire scheduled job {}: {}", schedule.getJobName(), e.getMessage());
        }
    }

    public ScheduleDocument registerSchedule(ScheduleDocument schedule) {
        schedule.setCreatedAt(Instant.now());
        schedule.setActive(true);
        return scheduleRepository.save(schedule);
    }

    public List<ScheduleDocument> getAllSchedules() {
        return scheduleRepository.findAll();
    }
}
