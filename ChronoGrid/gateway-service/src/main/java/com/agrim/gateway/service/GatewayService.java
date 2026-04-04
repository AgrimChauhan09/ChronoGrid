package com.agrim.gateway.service;

import com.agrim.gateway.model.ServiceRoute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

/**
 * GatewayService — resolves service routes and proxies requests.
 * Maps to pkg/gateway/gw.go in the Go project.
 */
@Service
public class GatewayService {

    private static final Logger log = LoggerFactory.getLogger(GatewayService.class);

    private final RestTemplate restTemplate;
    private final List<ServiceRoute> serviceRoutes;

    public GatewayService(RestTemplate restTemplate, List<ServiceRoute> serviceRoutes) {
        this.restTemplate = restTemplate;
        this.serviceRoutes = serviceRoutes;
    }

    public Optional<ServiceRoute> resolveRoute(String path) {
        return serviceRoutes.stream()
                .filter(r -> r.isEnabled() && path.startsWith(r.getPathPrefix()))
                .findFirst();
    }

    public ResponseEntity<String> forward(String targetUrl, HttpMethod method, Object body) {
        log.info("Forwarding {} request to {}", method, targetUrl);
        if (body != null && (method == HttpMethod.POST || method == HttpMethod.PUT)) {
            return restTemplate.postForEntity(targetUrl, body, String.class);
        }
        return restTemplate.getForEntity(targetUrl, String.class);
    }
}
