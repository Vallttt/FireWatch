package com.vallesol.bff.controller;

import com.vallesol.bff.client.AuthClient;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * BFF Proxy → Auth Service
 * Recibe requests del frontend (via Gateway) y las reenvía al microservicio de Auth.
 * Reenvía el código HTTP real del Auth Service (401, 400, etc.) para que el frontend
 * muestre mensajes de error apropiados.
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
        } catch (FeignException e) {
            // Auth Service retorna 401/403 para credenciales incorrectas
            int status = e.status();
            String message = "Correo o contraseña incorrectos";
            return ResponseEntity.status(401)
                    .body(Map.of("message", message));
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
        } catch (FeignException e) {
            int status = e.status();
            String message;
            if (status == 400) {
                message = "El correo ya se encuentra registrado";
            } else {
                message = "Error en el servicio de autenticación";
            }
            return ResponseEntity.status(status)
                    .body(Map.of("message", message));
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Auth Service no disponible", "message", e.getMessage()));
        }
    }
}
