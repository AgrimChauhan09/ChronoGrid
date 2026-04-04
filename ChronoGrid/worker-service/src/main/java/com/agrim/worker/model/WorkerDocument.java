package com.agrim.worker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * WorkerDocument — MongoDB document representing this worker instance.
 * Maps to pkg/worker/structs.go in the Go project.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "workers")
public class WorkerDocument {

    @Id
    private String workerId;

    private String host;

    private int port;

    private String status;

    private int activeJobs;

    private int maxConcurrentJobs;

    private long totalJobsExecuted;

    private long totalJobsFailed;

    private Instant registeredAt;

    private Instant lastHeartbeatAt;
}
