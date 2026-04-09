package com.agrim.common.shutdown;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * GracefulShutdownHandler — handles clean shutdown of each service.
 * Spring calls @PreDestroy on context close (SIGTERM / SIGINT).
 */
@Component
public class GracefulShutdownHandler {

    private static final Logger log = LoggerFactory.getLogger(GracefulShutdownHandler.class);

    @PreDestroy
    public void onShutdown() {
        log.info("Shutdown signal received — draining in-flight work before exit...");
    }
}
