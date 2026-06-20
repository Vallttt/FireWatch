package com.vallesol.bff.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@FeignClient(name = "evidence-service")
public interface EvidenceClient {

    @GetMapping("/api/evidencias/{reporteId}")
    List<Map<String, Object>> getEvidencias(@PathVariable("reporteId") String reporteId);

    @GetMapping("/api/evidencias/{reporteId}/count")
    Integer countEvidencias(@PathVariable("reporteId") String reporteId);

    @DeleteMapping("/api/evidencias/{reporteId}")
    void deleteEvidencias(@PathVariable("reporteId") String reporteId);
}
