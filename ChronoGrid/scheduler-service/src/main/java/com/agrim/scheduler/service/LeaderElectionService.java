package com.agrim.scheduler.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

/**
 * LeaderElectionService — ensures only one scheduler instance fires cron jobs.
 * Uses a MongoDB document as a distributed lock (TTL-based).
 * Maps to pkg/scheduler/leader_election.go in the Go project.
 */
@Service
public class LeaderElectionService {

    private static final Logger log = LoggerFactory.getLogger(LeaderElectionService.class);
    private static final String LEADER_COLLECTION = "leader_election";
    private static final String LOCK_KEY = "scheduler-leader";

    @Value("${spring.application.name:scheduler-instance-1}")
    private String instanceId;

    private final MongoTemplate mongoTemplate;

    public LeaderElectionService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public boolean isLeader() {
        try {
            Query query = new Query(Criteria.where("_id").is(LOCK_KEY)
                    .and("expiresAt").gt(Instant.now()));

            Update update = new Update()
                    .setOnInsert("_id", LOCK_KEY)
                    .setOnInsert("leaderId", instanceId)
                    .setOnInsert("expiresAt", Instant.now().plusSeconds(30));

            mongoTemplate.upsert(query, update, Map.class, LEADER_COLLECTION);

            Map<?, ?> doc = mongoTemplate.findOne(
                    new Query(Criteria.where("_id").is(LOCK_KEY)), Map.class, LEADER_COLLECTION);

            return doc != null && instanceId.equals(doc.get("leaderId"));
        } catch (Exception e) {
            log.warn("Leader election check failed: {}", e.getMessage());
            return false;
        }
    }

    public void renewLease() {
        mongoTemplate.updateFirst(
                new Query(Criteria.where("_id").is(LOCK_KEY).and("leaderId").is(instanceId)),
                new Update().set("expiresAt", Instant.now().plusSeconds(30)),
                Map.class, LEADER_COLLECTION);
    }
}
