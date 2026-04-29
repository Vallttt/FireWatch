package com.vallesol.bff.controller;

import com.vallesol.bff.client.AuthClient;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * BFF Proxy → Auth Service
 * Receives requests from the frontend (via Gateway) and forwards them to the Auth microservice.
 * Forwards the real HTTP status code from the Auth Service (401, 400, etc.) so the frontend
 * can display appropriate error messages.
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
            // Auth Service returns 401/403 for incorrect credentials
            int status = e.status();
            String message = "Incorrect email or password";
            return ResponseEntity.status(401)
                    .body(Map.of("message", message));
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Auth Service unavailable", "message", e.getMessage()));
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
                message = "The email is already registered";
            } else {
                message = "Authentication service error";
            }
            return ResponseEntity.status(status)
                    .body(Map.of("message", message));
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Auth Service unavailable", "message", e.getMessage()));
        }
    }
}
