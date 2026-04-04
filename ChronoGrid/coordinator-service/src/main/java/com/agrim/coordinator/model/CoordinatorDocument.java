package com.agrim.coordinator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * CoordinatorDocument — audit log of dispatch decisions.
 * Maps to pkg/worker-queue-coordinator/structs.go in the Go project.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "coordinator_dispatches")
public class CoordinatorDocument {

    @Id
    private String id;

    private String jobId;

    private String workerId;

    private String workerUrl;

    private String result;

    private Instant dispatchedAt;
}
