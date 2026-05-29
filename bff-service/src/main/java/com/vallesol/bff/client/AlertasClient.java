package com.vallesol.bff.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Feign Client → SolAlertas (:8083)
 * The BFF forwards alert and notification operations.
 */
@FeignClient(name = "ms-alertas", url = "${ms.alertas.url}")
public interface AlertasClient {

    @PostMapping("/api/alertas/enviar")
    String sendAlert(@RequestBody Map<String, Object> body);

    @GetMapping("/api/alertas")
    List<Map<String, Object>> getAlertHistory();

    @GetMapping("/api/alertas/conteo")
    Integer getTotalAlerts();

    @DeleteMapping("/api/alertas/{id}")
    void deleteAlert(@PathVariable("id") String id);
}
