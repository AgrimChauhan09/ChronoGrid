package com.agrim.exchange.controller;

import com.agrim.exchange.model.ExchangeMessage;
import com.agrim.exchange.model.ExchangeStats;
import com.agrim.exchange.service.ExchangeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * ExchangeController — HTTP interface for publishing events and registering subscribers.
 * Maps to cmd/exchange/main.go HTTP handlers.
 */
@RestController
@RequestMapping("/exchange")
public class ExchangeController {

    private final ExchangeService exchangeService;

    public ExchangeController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    @PostMapping("/publish")
    public ResponseEntity<String> publish(@RequestBody ExchangeMessage message) {
        exchangeService.publish(message);
        return ResponseEntity.ok("Published: " + message.getEventType());
    }

    @PostMapping("/subscribe")
    public ResponseEntity<String> subscribe(@RequestBody Map<String, String> body) {
        exchangeService.subscribe(body.get("eventType"), body.get("callbackUrl"));
        return ResponseEntity.ok("Subscribed to: " + body.get("eventType"));
    }

    @GetMapping("/stats")
    public ResponseEntity<ExchangeStats> stats() {
        return ResponseEntity.ok(exchangeService.getStats());
    }
}
