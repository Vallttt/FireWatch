package com.vallesol.bff.controller;

import com.vallesol.bff.client.EvidenceClient;
import com.vallesol.bff.client.IncidentClient;
import com.vallesol.bff.client.ReportesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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

    @Autowired
    private IncidentClient incidentClient;

    @Autowired
    private EvidenceClient evidenceClient;

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

    /**
     * Reporte completo (agregado): junta los datos base del reporte (report-service)
     * con su estado/severity (incident-service) y sus evidencias (evidence-service).
     * Best-effort: si incident o evidence fallan, devuelve el reporte igual con los
     * campos disponibles (estado/severity en null, evidencias vacias).
     */
    @GetMapping("/{id}/completo")
    public ResponseEntity<?> getReporteCompleto(@PathVariable String id) {
        Map<String, Object> reporte;
        try {
            reporte = reportesClient.getReport(id);
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Report Service unavailable", "message", e.getMessage()));
        }

        Map<String, Object> completo = new HashMap<>(reporte);

        // Agregar estado y severity (incident-service)
        try {
            Map<String, Object> incidente = incidentClient.getByReport(id);
            completo.put("estado", incidente.get("estado"));
            completo.put("severity", incidente.get("severity"));
        } catch (Exception e) {
            completo.put("estado", null);
            completo.put("severity", null);
        }

        // agregar foto evidencias (evidence-service)
        try {
            List<Map<String, Object>> evidencias = evidenceClient.getEvidencias(id);
            completo.put("evidencias", evidencias);
        } catch (Exception e) {
            completo.put("evidencias", List.of());
        }

        return ResponseEntity.ok(completo);
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
