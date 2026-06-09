package com.vallesol.bff.controller;

import com.vallesol.bff.client.IncidentClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * BFF Proxy → Incident Service
 * Recibe peticiones del frontend (via Gateway) y las reenvia al microservicio de Incidentes.
 */
@RestController
@RequestMapping("/api/incidentes")
public class IncidentProxyController {

    @Autowired
    private IncidentClient incidentClient;

    @GetMapping
    public ResponseEntity<?> listIncidents() {
        try {
            List<Map<String, Object>> response = incidentClient.listIncidents();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Incident Service unavailable", "message", e.getMessage()));
        }
    }

    @GetMapping("/focos-activos")
    public ResponseEntity<?> getActiveFires() {
        try {
            Integer total = incidentClient.getTotalActiveFires();
            return ResponseEntity.ok(total != null ? total : 0);
        } catch (Exception e) {
            return ResponseEntity.ok(0);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getIncident(@PathVariable String id) {
        try {
            Map<String, Object> response = incidentClient.getIncident(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Incident Service unavailable", "message", e.getMessage()));
        }
    }

    @GetMapping("/reporte/{reporteId}")
    public ResponseEntity<?> getByReport(@PathVariable String reporteId) {
        try {
            Map<String, Object> response = incidentClient.getByReport(reporteId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Incident Service unavailable", "message", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> updateStatus(@PathVariable String id, @RequestBody Map<String, Object> body) {
        try {
            Map<String, Object> response = incidentClient.updateStatus(id, body);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Incident Service unavailable", "message", e.getMessage()));
        }
    }
}
