package com.vallesol.bff.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;


@FeignClient(name = "notification-service")
public interface NotificacionesClient {

    @GetMapping("/api/notificaciones")
    List<Map<String, Object>> getHistory();

    @GetMapping("/api/notificaciones/conteo")
    Integer getTotalCount();

    @DeleteMapping("/api/notificaciones/{id}")
    void deleteById(@PathVariable("id") String id);
}
