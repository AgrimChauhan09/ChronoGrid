package com.agrim.gateway.service;

import com.agrim.gateway.model.ServiceRoute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
/**
 * GatewayService — resolves service routes and proxies requests.
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

    public ResponseEntity<String> forward(String path, HttpMethod method, Object body) {
        Optional<ServiceRoute> routeOpt = resolveRoute(path);

        if (routeOpt.isEmpty()) {
            log.warn("No route found for path: {}", path);
            return ResponseEntity.status(404).body("Route not found for path: " + path);
        }

        String fullTargetUrl = routeOpt.get().getTargetBaseUrl() + path;

        log.info("Forwarding {} request to {}", method, fullTargetUrl);

        try {
            HttpEntity<Object> requestEntity = new HttpEntity<>(body);
            return restTemplate.exchange(fullTargetUrl, method, requestEntity, String.class);
        } catch (Exception e) {
            log.error("Failed to forward request to {}: {}", fullTargetUrl, e.getMessage());
            return ResponseEntity.status(500).body("Error forwarding request to backend service.");
        }
    }
}