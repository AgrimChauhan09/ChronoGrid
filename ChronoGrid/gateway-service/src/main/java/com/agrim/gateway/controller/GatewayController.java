package com.agrim.gateway.controller;

import com.agrim.gateway.model.ServiceRoute;
import com.agrim.gateway.service.GatewayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * GatewayController — catches all inbound requests and routes them.
 * Maps to pkg/gateway/gw.go in the Go project.
 */
@RestController
@RequestMapping("/api")
public class GatewayController {

    private final GatewayService gatewayService;

    public GatewayController(GatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    @RequestMapping("/**")
    public ResponseEntity<String> route(HttpServletRequest request,
            @RequestBody(required = false) Object body) {
        String path = request.getRequestURI();
        HttpMethod method = HttpMethod.valueOf(request.getMethod());

        Optional<ServiceRoute> route = gatewayService.resolveRoute(path);

        if (route.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String targetUrl = route.get().getTargetBaseUrl() + path;
        return gatewayService.forward(targetUrl, method, body);
    }
}
