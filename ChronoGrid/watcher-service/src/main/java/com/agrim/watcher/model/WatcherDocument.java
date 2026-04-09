package com.agrim.watcher.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * WatcherDocument — audit record of watcher interventions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "watcher_events")
public class WatcherDocument {

    @Id
    private String id;

    private String jobId;

    private String action;

    private String reason;

    private Instant detectedAt;
}
