package com.agrim.common.dto;

import com.agrim.common.model.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobResponse {

    private String id;

    private String name;

    private String type;

    private JobStatus status;

    private String assignedWorkerId;

    private int retryCount;

    private Instant createdAt;

    private Instant scheduledAt;

    private Instant startedAt;

    private Instant completedAt;

    private String errorMessage;
}
