package com.agrim.scheduler.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * ScheduleDocument — MongoDB document for a registered cron schedule.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "schedules")
public class ScheduleDocument {

    @Id
    private String id;

    private String jobType;

    private String jobName;

    private String cronExpression;

    private boolean active;

    private Instant lastTriggeredAt;

    private Instant nextTriggerAt;

    private Instant createdAt;
}
