package com.agrim.gateway.config;

import com.agrim.gateway.model.ServiceRoute;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * GatewayConfig — registers all known backend service routes.
 * Maps to pkg/gateway/structs.go route definitions.
 */
@Configuration
public class GatewayConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public List<ServiceRoute> serviceRoutes() {
        return List.of(
            ServiceRoute.builder().serviceName("job-scheduler").pathPrefix("/jobs").targetBaseUrl("http://localhost:8087").enabled(true).build(),
            ServiceRoute.builder().serviceName("scheduler").pathPrefix("/schedule").targetBaseUrl("http://localhost:8081").enabled(true).build(),
            ServiceRoute.builder().serviceName("worker").pathPrefix("/workers").targetBaseUrl("http://localhost:8082").enabled(true).build(),
            ServiceRoute.builder().serviceName("queue").pathPrefix("/queue").targetBaseUrl("http://localhost:8085").enabled(true).build(),
            ServiceRoute.builder().serviceName("coordinator").pathPrefix("/coordinator").targetBaseUrl("http://localhost:8086").enabled(true).build(),
            ServiceRoute.builder().serviceName("watcher").pathPrefix("/watcher").targetBaseUrl("http://localhost:8083").enabled(true).build()
        );
    }
}
