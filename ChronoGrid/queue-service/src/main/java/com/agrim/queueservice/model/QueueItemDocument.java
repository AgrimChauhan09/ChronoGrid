package com.agrim.queueservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * QueueItemDocument — MongoDB-persisted queue entry for durability.
 * Maps to pkg/queue/structs.go in the Go project.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "job_queue")
public class QueueItemDocument {

    @Id
    private String id;

    private String jobId;

    private String jobType;

    private String status;

    private int priority;

    private Instant enqueuedAt;

    private Instant dequeuedAt;
}
