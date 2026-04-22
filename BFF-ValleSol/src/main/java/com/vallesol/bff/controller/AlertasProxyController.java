package com.vallesol.bff.controller;

import com.vallesol.bff.client.AlertasClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * BFF Proxy → SolAlertas Service
 * Recibe requests del frontend (via Gateway) y las reenvía al microservicio de Alertas.
 */
@RestController
@RequestMapping("/api/alertas")
public class AlertasProxyController {

    @Autowired
    private AlertasClient alertasClient;

    @PostMapping("/enviar")
    public ResponseEntity<?> enviarAlerta(@RequestBody Map<String, Object> body) {
        try {
            String response = alertasClient.enviarAlerta(body);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Alertas Service no disponible", "message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> historialAlertas() {
        try {
            List<Map<String, Object>> response = alertasClient.historialAlertas();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Alertas Service no disponible", "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarAlerta(@PathVariable String id) {
        try {
            alertasClient.eliminarAlerta(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Alertas Service no disponible", "message", e.getMessage()));
        }
    }
}
