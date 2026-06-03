package com.vallesol.bff.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;


@FeignClient(name = "alert-service")
public interface AlertasClient {

    @PostMapping("/api/alertas/enviar")
    String sendAlert(@RequestBody Map<String, Object> body);
}
