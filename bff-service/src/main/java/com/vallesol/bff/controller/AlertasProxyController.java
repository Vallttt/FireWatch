package com.vallesol.bff.controller;

import com.vallesol.bff.client.AlertasClient;
import com.vallesol.bff.client.NotificacionesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * BFF Proxy for alert and notification operations.
 *
 * The frontend always talks to /api/alertas/* — the BFF transparently routes:
 *   POST /enviar   → alert-service      (creates the Alerta + triggers notification fan-out)
 *   GET  /         → notification-service (notification history, one row per dispatch)
 *   DEL  /{id}     → notification-service (deletes a full dispatch group)
 */
@RestController
@RequestMapping("/api/alertas")
public class AlertasProxyController {

    @Autowired
    private AlertasClient alertasClient;

    @Autowired
    private NotificacionesClient notificacionesClient;

    /** Triggers alert creation in alert-service. */
    @PostMapping("/enviar")
    public ResponseEntity<?> sendAlert(@RequestBody Map<String, Object> body) {
        try {
            String response = alertasClient.sendAlert(body);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Alert Service unavailable", "message", e.getMessage()));
        }
    }

    /** Returns notification history from notification-service. */
    @GetMapping
    public ResponseEntity<?> alertHistory() {
        try {
            List<Map<String, Object>> response = notificacionesClient.getHistory();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Notification Service unavailable", "message", e.getMessage()));
        }
    }

    /** Deletes a notification dispatch group from notification-service. */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAlert(@PathVariable String id) {
        try {
            notificacionesClient.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Notification Service unavailable", "message", e.getMessage()));
        }
    }
}
