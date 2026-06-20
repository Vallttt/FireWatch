package com.vallesol.bff.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@FeignClient(name = "incident-service")
public interface IncidentClient {

    @GetMapping("/api/incidentes")
    List<Map<String, Object>> listIncidents();

    @GetMapping("/api/incidentes/focos-activos")
    Integer getTotalActiveFires();

    @GetMapping("/api/incidentes/{id}")
    Map<String, Object> getIncident(@PathVariable("id") String id);

    @GetMapping("/api/incidentes/reporte/{reporteId}")
    Map<String, Object> getByReport(@PathVariable("reporteId") String reporteId);

    @PatchMapping("/api/incidentes/{id}/estado")
    Map<String, Object> updateStatus(@PathVariable("id") String id, @RequestBody Map<String, Object> body);
}
