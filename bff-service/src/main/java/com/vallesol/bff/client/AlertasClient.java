package com.vallesol.bff.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * Feign Client → alert-service (registered in Eureka as "alert-service").
 *
 * Responsibility: trigger alert creation only.
 * History, count, and delete operations are handled by NotificacionesClient
 * because those records live in notification-service.
 */
@FeignClient(name = "alert-service")
public interface AlertasClient {

    @PostMapping("/api/alertas/enviar")
    String sendAlert(@RequestBody Map<String, Object> body);
}
