package com.vallesol.bff.controller;

import com.vallesol.bff.client.GeoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * BFF Proxy → Geo Service
 * Recibe requests del frontend (via Gateway) y las reenvía al microservicio de Geolocalización.
 * El frontend llama /api/mapa/**, el BFF traduce a las rutas del Geo Service.
 */
@RestController
@RequestMapping("/api/mapa")
public class GeoProxyController {

    @Autowired
    private GeoClient geoClient;

    /* ===== DATOS DEL MAPA ===== */
    @GetMapping("/api/map-data")
    public ResponseEntity<?> getMapData() {
        try {
            return ResponseEntity.ok(geoClient.getMapData());
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Geo Service no disponible", "message", e.getMessage()));
        }
    }

    /* ===== BRIGADAS ===== */
    @GetMapping("/api/brigades")
    public ResponseEntity<?> getBrigades() {
        try {
            return ResponseEntity.ok(geoClient.getBrigades());
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Geo Service no disponible", "message", e.getMessage()));
        }
    }

    @PostMapping("/api/brigades")
    public ResponseEntity<?> createBrigade(@RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(geoClient.createBrigade(body));
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Geo Service no disponible", "message", e.getMessage()));
        }
    }

    @GetMapping("/api/brigades/{id}")
    public ResponseEntity<?> getBrigade(@PathVariable String id) {
        try {
            return ResponseEntity.ok(geoClient.getBrigade(id));
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Geo Service no disponible", "message", e.getMessage()));
        }
    }

    @PutMapping("/api/brigades/{id}")
    public ResponseEntity<?> updateBrigade(@PathVariable String id, @RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(geoClient.updateBrigade(id, body));
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Geo Service no disponible", "message", e.getMessage()));
        }
    }

    @DeleteMapping("/api/brigades/{id}")
    public ResponseEntity<?> deleteBrigade(@PathVariable String id) {
        try {
            return ResponseEntity.ok(geoClient.deleteBrigade(id));
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Geo Service no disponible", "message", e.getMessage()));
        }
    }

    /* ===== REPORTES MAPEADOS ===== */
    @GetMapping("/api/mapped-reports")
    public ResponseEntity<?> getMappedReports() {
        try {
            return ResponseEntity.ok(geoClient.getMappedReports());
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Geo Service no disponible", "message", e.getMessage()));
        }
    }

    @PostMapping("/api/mapped-reports")
    public ResponseEntity<?> createMappedReport(@RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(geoClient.createMappedReport(body));
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Geo Service no disponible", "message", e.getMessage()));
        }
    }

    @PutMapping("/api/mapped-reports/{id}")
    public ResponseEntity<?> updateMappedReport(@PathVariable String id, @RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(geoClient.updateMappedReport(id, body));
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Geo Service no disponible", "message", e.getMessage()));
        }
    }

    @DeleteMapping("/api/mapped-reports/{id}")
    public ResponseEntity<?> deleteMappedReport(@PathVariable String id) {
        try {
            return ResponseEntity.ok(geoClient.deleteMappedReport(id));
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Geo Service no disponible", "message", e.getMessage()));
        }
    }

    /* ===== ZONAS ===== */
    @GetMapping("/api/zones")
    public ResponseEntity<?> getZones() {
        try {
            return ResponseEntity.ok(geoClient.getZones());
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Geo Service no disponible", "message", e.getMessage()));
        }
    }

    /* ===== rutas de evacuación ===== */
    @GetMapping("/api/evacroute")
    public ResponseEntity<?> getEvacuationRoutes() {
        try {
            return ResponseEntity.ok(geoClient.getEvacuationRoutes());
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Geo Service no disponible", "message", e.getMessage()));
        }
    }
}
