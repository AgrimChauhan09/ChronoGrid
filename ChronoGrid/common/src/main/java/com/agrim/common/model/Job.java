package com.agrim.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "jobs")
public class Job {

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

    private Instant createdAt;

    private Instant updatedAt;

    private Instant scheduledAt;

    private Instant startedAt;

    private Instant completedAt;

    private String errorMessage;
}
