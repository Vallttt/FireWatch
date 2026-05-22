package com.vallesol.bff.controller;

import com.vallesol.bff.client.AlertasClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * BFF Proxy → SolAlertas Service
 * Receives requests from the frontend (via Gateway) and forwards them to the Alerts microservice.
 */
@RestController
@RequestMapping("/api/alertas")
public class AlertasProxyController {

    @Autowired
    private AlertasClient alertasClient;

    @PostMapping("/enviar")
    public ResponseEntity<?> sendAlert(@RequestBody Map<String, Object> body) {
        try {
            String response = alertasClient.sendAlert(body);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Alerts Service unavailable", "message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> alertHistory() {
        try {
            List<Map<String, Object>> response = alertasClient.getAlertHistory();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Alerts Service unavailable", "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAlert(@PathVariable String id) {
        try {
            alertasClient.deleteAlert(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Alerts Service unavailable", "message", e.getMessage()));
        }
    }
}
