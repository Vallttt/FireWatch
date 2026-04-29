package com.vallesol.bff.controller;

import com.vallesol.bff.client.GeoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * BFF Proxy → Geo Service
 * Receives requests from the frontend (via Gateway) and forwards them to the Geolocation microservice.
 * The frontend calls /api/mapa/**, the BFF translates to the Geo Service routes.
 */
@RestController
@RequestMapping("/api/mapa")
public class GeoProxyController {

    @Autowired
    private GeoClient geoClient;

    /* ===== MAP DATA ===== */
    @GetMapping("/map-data")
    public ResponseEntity<?> getMapData() {
        try {
            return ResponseEntity.ok(geoClient.getMapData());
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Geo Service unavailable", "message", e.getMessage()));
        }
    }

    /* ===== BRIGADES ===== */
    @GetMapping("/brigades")
    public ResponseEntity<?> getBrigades() {
        try {
            return ResponseEntity.ok(geoClient.getBrigades());
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Geo Service unavailable", "message", e.getMessage()));
        }
    }

    @PostMapping("/brigades")
    public ResponseEntity<?> createBrigade(@RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(geoClient.createBrigade(body));
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Geo Service unavailable", "message", e.getMessage()));
        }
    }

    @GetMapping("/brigades/{id}")
    public ResponseEntity<?> getBrigade(@PathVariable String id) {
        try {
            return ResponseEntity.ok(geoClient.getBrigade(id));
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Geo Service unavailable", "message", e.getMessage()));
        }
    }

    @PutMapping("/brigades/{id}")
    public ResponseEntity<?> updateBrigade(@PathVariable String id, @RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(geoClient.updateBrigade(id, body));
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Geo Service unavailable", "message", e.getMessage()));
        }
    }

    @DeleteMapping("/brigades/{id}")
    public ResponseEntity<?> deleteBrigade(@PathVariable String id) {
        try {
            return ResponseEntity.ok(geoClient.deleteBrigade(id));
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Geo Service unavailable", "message", e.getMessage()));
        }
    }

    /* ===== MAPPED REPORTS ===== */
    @GetMapping("/mapped-reports")
    public ResponseEntity<?> getMappedReports() {
        try {
            return ResponseEntity.ok(geoClient.getMappedReports());
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Geo Service unavailable", "message", e.getMessage()));
        }
    }

    @PostMapping("/mapped-reports")
    public ResponseEntity<?> createMappedReport(@RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(geoClient.createMappedReport(body));
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Geo Service unavailable", "message", e.getMessage()));
        }
    }

    @PutMapping("/mapped-reports/{id}")
    public ResponseEntity<?> updateMappedReport(@PathVariable String id, @RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(geoClient.updateMappedReport(id, body));
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Geo Service unavailable", "message", e.getMessage()));
        }
    }

    @DeleteMapping("/mapped-reports/{id}")
    public ResponseEntity<?> deleteMappedReport(@PathVariable String id) {
        try {
            return ResponseEntity.ok(geoClient.deleteMappedReport(id));
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Geo Service unavailable", "message", e.getMessage()));
        }
    }

    /* ===== ZONES ===== */
    @GetMapping("/zones")
    public ResponseEntity<?> getZones() {
        try {
            return ResponseEntity.ok(geoClient.getZones());
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Geo Service unavailable", "message", e.getMessage()));
        }
    }

    /* ===== EVACUATION ROUTES ===== */
    @GetMapping("/evacroute")
    public ResponseEntity<?> getEvacuationRoutes() {
        try {
            return ResponseEntity.ok(geoClient.getEvacuationRoutes());
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Geo Service unavailable", "message", e.getMessage()));
        }
    }
}
