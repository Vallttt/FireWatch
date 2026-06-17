package com.vallesol.bff.controller;

import com.vallesol.bff.client.ReportesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * BFF Proxy → Report Service
 * Recibe peticiones del frontend (via Gateway) y las reenvia al microservicio de Reportes.
 */
@RestController
@RequestMapping("/api/reportes")
public class ReportesProxyController {

    @Autowired
    private ReportesClient reportesClient;

    @PostMapping
    public ResponseEntity<?> createReport(@RequestBody Map<String, Object> body) {
        try {
            Map<String, Object> response = reportesClient.createReport(body);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Report Service unavailable", "message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> listReports() {
        try {
            List<Map<String, Object>> response = reportesClient.listReports();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Report Service unavailable", "message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReport(@PathVariable String id) {
        try {
            Map<String, Object> response = reportesClient.getReport(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Report Service unavailable", "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReport(@PathVariable String id) {
        try {
            reportesClient.deleteReport(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Report Service unavailable", "message", e.getMessage()));
        }
    }
}
