package com.agrim.gateway.controller;

import com.agrim.gateway.service.GatewayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
/**
 * GatewayController — catches all inbound requests and routes them.
 */
@RestController
public class GatewayController {

    private final GatewayService gatewayService;

    public GatewayController(GatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    @RequestMapping("/api")
    public ResponseEntity<String> route(HttpServletRequest request,
                                        @RequestBody(required = false) Object body) {

        String path = request.getRequestURI();
        if (path.startsWith("/api")) {
            path = path.substring(4);
        }

        HttpMethod method = HttpMethod.valueOf(request.getMethod());

        return gatewayService.forward(path, method, body);
    }
}