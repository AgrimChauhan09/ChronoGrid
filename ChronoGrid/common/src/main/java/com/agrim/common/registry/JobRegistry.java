package com.agrim.common.registry;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JobRegistry — tracks all known job types and their handlers.
 * Maps to pkg/job-registry in the Go project.
 */
@Component
public class JobRegistry {

    private final Map<String, String> jobTypeToHandlerMap = new ConcurrentHashMap<>();

    public void register(String jobType, String handlerClass) {
        jobTypeToHandlerMap.put(jobType, handlerClass);
    }

    public String getHandler(String jobType) {
        return jobTypeToHandlerMap.get(jobType);
    }

    public boolean isRegistered(String jobType) {
        return jobTypeToHandlerMap.containsKey(jobType);
    }

    public Set<String> getAllJobTypes() {
        return jobTypeToHandlerMap.keySet();
    }

    public void deregister(String jobType) {
        jobTypeToHandlerMap.remove(jobType);
    }
}
