package com.agrim.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobRequest {

    private String name;

    private String type;

    private String cronExpression;

    private Map<String, Object> payload;

    private int maxRetries;

    private long timeoutSeconds;
}
