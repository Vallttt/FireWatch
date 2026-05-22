package com.vallesol.bff.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Feign Client → Report Service (:8081)
 * The BFF forwards emergency report operations.
 */
@FeignClient(name = "ms-reportes", url = "${ms.reportes.url}")
public interface ReportesClient {

    @PostMapping("/api/reportes")
    Map<String, Object> createReport(@RequestBody Map<String, Object> body);

    @GetMapping("/api/reportes")
    List<Map<String, Object>> listReports();

    @GetMapping("/api/reportes/{id}")
    Map<String, Object> getReport(@PathVariable("id") String id);

    @PatchMapping("/api/reportes/{id}/estado")
    Map<String, Object> updateStatus(@PathVariable("id") String id, @RequestBody Map<String, Object> body);

    @GetMapping("/api/reportes/focos-activos")
    Integer getTotalActiveFires();

    @DeleteMapping("/api/reportes/{id}")
    void deleteReport(@PathVariable("id") String id);

    @GetMapping("/api/reportes/{id}/media")
    java.util.List<Map<String, Object>> getMedia(@PathVariable("id") String id);
}
