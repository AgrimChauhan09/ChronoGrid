package com.agrim.coordinator.config;

import com.agrim.common.loadbalancer.LoadBalancer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class CoordinatorConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public LoadBalancer loadBalancer() {
        return new LoadBalancer();
    }
}
