package com.ValleSol.apiGateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * API Gateway — JWT Security Configuration
 *
 * Validates Bearer tokens on every request except the explicitly public paths.
 * The same HMAC-SHA256 secret used by auth-service to sign tokens is used here
 * to verify them, avoiding any round-trip call to auth-service at runtime.
 *
 * Public paths (no token required):
 *   /auth/**                — login
 *   /api/users/register     — user registration
 *   /api/auth/password/**   — password forgot / reset
 *   OPTIONS /**             — CORS preflight requests
 *
 * All other requests must carry a valid Authorization: Bearer <token> header.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeExchange(auth -> auth
                        // CORS preflight — always allow
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Public authentication endpoints
                        .pathMatchers("/auth/**").permitAll()
                        // Public registration
                        .pathMatchers("/api/users/register").permitAll()
                        // Password recovery flow
                        .pathMatchers("/api/auth/password/**").permitAll()
                        // Every other request needs a valid JWT
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtDecoder(jwtDecoder()))
                )
                .build();
    }

    /**
     * Builds a reactive JWT decoder using the same HMAC-SHA256 secret as auth-service.
     * No network call is made — validation is done locally with the shared secret.
     */
    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusReactiveJwtDecoder
                .withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}
