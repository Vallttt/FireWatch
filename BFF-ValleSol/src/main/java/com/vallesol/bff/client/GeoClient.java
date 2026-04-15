package com.vallesol.bff.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Feign Client → Geo Service (:8082)
 * El BFF reenvía operaciones de mapa, brigadas, zonas y reportes mapeados.
 */
@FeignClient(name = "ms-geo", url = "${ms.geo.url}")
public interface GeoClient {

    /* ===== MAP DATA ===== */
    @GetMapping("/api/map-data")
    Map<String, Object> getMapData();

    /* ===== BRIGADES ===== */
    @GetMapping("/api/brigades")
    Map<String, Object> getBrigades();

    @PostMapping("/api/brigades")
    Map<String, Object> createBrigade(@RequestBody Map<String, Object> body);

    @GetMapping("/api/brigades/{id}")
    Map<String, Object> getBrigade(@PathVariable("id") String id);

    @PutMapping("/api/brigades/{id}")
    Map<String, Object> updateBrigade(@PathVariable("id") String id, @RequestBody Map<String, Object> body);

    @DeleteMapping("/api/brigades/{id}")
    Map<String, Object> deleteBrigade(@PathVariable("id") String id);

    /* ===== MAPPED REPORTS ===== */
    @GetMapping("/api/mapped-reports")
    Map<String, Object> getMappedReports();

    @PostMapping("/api/mapped-reports")
    Map<String, Object> createMappedReport(@RequestBody Map<String, Object> body);

    @GetMapping("/api/mapped-reports/{id}")
    Map<String, Object> getMappedReport(@PathVariable("id") String id);

    @PutMapping("/api/mapped-reports/{id}")
    Map<String, Object> updateMappedReport(@PathVariable("id") String id, @RequestBody Map<String, Object> body);

    @DeleteMapping("/api/mapped-reports/{id}")
    Map<String, Object> deleteMappedReport(@PathVariable("id") String id);

    /* ===== ZONES ===== */
    @GetMapping("/api/zones")
    Map<String, Object> getZones();

    @GetMapping("/api/zones/{id}")
    Map<String, Object> getZone(@PathVariable("id") String id);

    /* ===== EVACUATION ROUTES ===== */
    @GetMapping("/api/evacroute")
    Map<String, Object> getEvacuationRoutes();
}
