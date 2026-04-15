package com.vallesol.bff.controller;

import com.vallesol.bff.client.AuthClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * BFF Proxy → Auth Service
 * Recibe requests del frontend (via Gateway) y las reenvía al microservicio de Auth.
 */
@RestController
@RequestMapping("/auth")
public class AuthProxyController {

    @Autowired
    private AuthClient authClient;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, Object> body) {
        try {
            Map<String, Object> response = authClient.login(body);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Auth Service no disponible", "message", e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> body) {
        try {
            Map<String, Object> response = authClient.register(body);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Auth Service no disponible", "message", e.getMessage()));
        }
    }
}
