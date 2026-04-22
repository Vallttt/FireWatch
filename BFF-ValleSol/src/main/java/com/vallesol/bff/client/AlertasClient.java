package com.vallesol.bff.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Feign Client → SolAlertas (:8083)
 * El BFF reenvía operaciones de alertas y notificaciones.
 */
@FeignClient(name = "ms-alertas", url = "${ms.alertas.url}")
public interface AlertasClient {

    @PostMapping("/api/alertas/enviar")
    String enviarAlerta(@RequestBody Map<String, Object> body);

    @GetMapping("/api/alertas")
    List<Map<String, Object>> historialAlertas();

    @GetMapping("/api/alertas/conteo")
    Integer obtenerTotalAlertas();

    @DeleteMapping("/api/alertas/{id}")
    void eliminarAlerta(@PathVariable("id") String id);
}
