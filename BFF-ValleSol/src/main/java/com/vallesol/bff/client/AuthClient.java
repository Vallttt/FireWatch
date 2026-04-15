package com.vallesol.bff.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * Feign Client → Auth Service (:8080)
 * El BFF reenvía login y register al microservicio de autenticación.
 */
@FeignClient(name = "ms-auth", url = "${ms.auth.url}")
public interface AuthClient {

    @PostMapping("/auth/login")
    Map<String, Object> login(@RequestBody Map<String, Object> body);

    @PostMapping("/auth/register")
    Map<String, Object> register(@RequestBody Map<String, Object> body);
}
