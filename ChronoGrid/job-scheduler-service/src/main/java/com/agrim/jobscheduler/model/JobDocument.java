package com.agrim.jobscheduler.model;

import com.agrim.common.model.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

/**
 * JobDocument — persisted MongoDB record for every submitted job.
 * This is the source of truth for job state across the system.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "jobs")
public class JobDocument {

    @Id
    private String id;

    private String name;

    private String type;

    private String cronExpression;

    private JobStatus status;

    private Map<String, Object> payload;

    private int retryCount;

    private int maxRetries;

    private long timeoutSeconds;

    private String assignedWorkerId;

    private String errorMessage;

    private Instant createdAt;

    private Instant updatedAt;

    private Instant scheduledAt;

    private Instant startedAt;

    private Instant completedAt;
}
