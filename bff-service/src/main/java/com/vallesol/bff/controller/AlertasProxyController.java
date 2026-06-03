package com.vallesol.bff.controller;

import com.vallesol.bff.client.AlertasClient;
import com.vallesol.bff.client.NotificacionesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/alertas")
public class AlertasProxyController {

    @Autowired
    private AlertasClient alertasClient;

    @Autowired
    private NotificacionesClient notificacionesClient;

   
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
