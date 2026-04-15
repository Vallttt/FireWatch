package com.vallesol.bff.controller;

import com.vallesol.bff.client.ReportesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * BFF Proxy → Report Service
 * Recibe requests del frontend (via Gateway) y las reenvía al microservicio de Reportes.
 */
@RestController
@RequestMapping("/api/reportes")
public class ReportesProxyController {

    @Autowired
    private ReportesClient reportesClient;

    @PostMapping
    public ResponseEntity<?> crearReporte(@RequestBody Map<String, Object> body) {
        try {
            Map<String, Object> response = reportesClient.crearReporte(body);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Report Service no disponible", "message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> listarReportes() {
        try {
            List<Map<String, Object>> response = reportesClient.listarReportes();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Report Service no disponible", "message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerReporte(@PathVariable String id) {
        try {
            Map<String, Object> response = reportesClient.obtenerReporte(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Report Service no disponible", "message", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstado(@PathVariable String id, @RequestBody Map<String, Object> body) {
        try {
            Map<String, Object> response = reportesClient.actualizarEstado(id, body);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Report Service no disponible", "message", e.getMessage()));
        }
    }
}
