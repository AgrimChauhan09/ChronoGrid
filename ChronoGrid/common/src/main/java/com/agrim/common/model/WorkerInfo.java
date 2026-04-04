package com.agrim.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "workers")
public class WorkerInfo {

    @Id
    private String workerId;

    private String host;

    private int port;

    private WorkerStatus status;

    private int activeJobs;

    private int maxConcurrentJobs;

    private Instant registeredAt;

    private Instant lastHeartbeatAt;

    public enum WorkerStatus {
        IDLE,
        BUSY,
        OFFLINE
    }
}
