package com.vallesol.bff.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Feign Client → Report Service (:8081)
 * El BFF reenvía operaciones de reportes de emergencia.
 */
@FeignClient(name = "ms-reportes", url = "${ms.reportes.url}")
public interface ReportesClient {

    @PostMapping("/api/reportes")
    Map<String, Object> crearReporte(@RequestBody Map<String, Object> body);

    @GetMapping("/api/reportes")
    List<Map<String, Object>> listarReportes();

    @GetMapping("/api/reportes/{id}")
    Map<String, Object> obtenerReporte(@PathVariable("id") String id);

    @PatchMapping("/api/reportes/{id}/estado")
    Map<String, Object> actualizarEstado(@PathVariable("id") String id, @RequestBody Map<String, Object> body);

    @GetMapping("/api/reportes/focos-activos")
    Integer obtenerTotalFocosActivos();
}
