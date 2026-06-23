package com.ValleSol.apiGateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.List;

/**
 * API Gateway — JWT Security Configuration
 *
 * Valida el token (firma + expiración) con el mismo secreto HMAC-SHA256 que
 * usa auth-service, y además mapea el claim "role" del JWT (ADMIN | USER) a
 * una autoridad de Spring Security ("ROLE_ADMIN" / "ROLE_USER") para poder
 * restringir rutas de escritura/administración solo a ADMIN.
 *
 * Público (sin token):
 *   /auth/**                  — login
 *   /api/users/register       — registro
 *   /api/auth/password/**     — recuperación de contraseña
 *   POST /api/reportes        — reporte anónimo (ciudadano sin cuenta, "Anonimo")
 *   POST /api/evidencias/**   — foto/video adjunto al reporte anónimo
 *   GET /api/zones/**, GET|POST /api/evacuation-routes/** — el reporte anónimo
 *     dispara internamente la resolución de zona y la ruta de evacuación
 *   OPTIONS /**                — preflight CORS
 *
 * Solo ADMIN (requiere rol ADMIN además de token válido):
 *   /api/users/**                          (gestión de usuarios)
 *   POST|PUT|DELETE /api/zones/**          (CRUD de zonas)
 *   POST|PUT|DELETE /api/brigades/**       (CRUD de brigadas)
 *   PUT|DELETE /api/evacuation-routes/**   (editar/eliminar rutas)
 *   PATCH /api/incidentes/**               (cambiar estado de un incidente)
 *   DELETE /api/reportes/**                (eliminar reporte)
 *   DELETE /api/evidencias/**              (eliminar evidencias)
 *   DELETE /api/geo/**                     (quitar incendio finalizado del mapa)
 *   POST|DELETE /api/alertas/**            (emitir / eliminar alertas)
 *
 * Cualquier otro request — solo requiere un JWT válido (USER o ADMIN).
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
                        // CORS preflight — siempre permitido
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ── Público ──────────────────────────────────────────────
                        .pathMatchers("/auth/**").permitAll()
                        .pathMatchers("/api/users/register").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/users/password/forgot").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/users/password/reset").permitAll()
                        .pathMatchers("/api/auth/password/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/reportes").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/evidencias/**").permitAll()
                        // El ciudadano anónimo ("modo emergencia", sin cuenta/token) también
                        // dispara internamente: resolver su zona real (GET zones) y la
                        // generación automática de su ruta de evacuación (GET+POST
                        // evacuation-routes). Sin esto, reportar sin sesión queda roto.
                        .pathMatchers(HttpMethod.GET,  "/api/zones/**").permitAll()
                        .pathMatchers(HttpMethod.GET,  "/api/evacuation-routes/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/evacuation-routes/**").permitAll()

                        // ── Solo ADMIN ───────────────────────────────────────────
                        .pathMatchers("/api/users/**").hasRole("ADMIN")

                        .pathMatchers(HttpMethod.POST,   "/api/zones/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT,    "/api/zones/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/zones/**").hasRole("ADMIN")

                        .pathMatchers(HttpMethod.POST,   "/api/brigades/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT,    "/api/brigades/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/brigades/**").hasRole("ADMIN")

                        // POST queda abierto a cualquier usuario autenticado (no solo ADMIN):
                        // la ruta de evacuación se genera sola, desde la sesión del propio
                        // ciudadano, al registrar su reporte (no hay creación manual por el
                        // admin). Editar/eliminar rutas sigue siendo exclusivo de ADMIN.
                        .pathMatchers(HttpMethod.PUT,    "/api/evacuation-routes/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/evacuation-routes/**").hasRole("ADMIN")

                        .pathMatchers(HttpMethod.PATCH,  "/api/incidentes/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/reportes/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/evidencias/**").hasRole("ADMIN")
                        // Borrado lógico del incendio en el mapa al marcarlo "Finalizado" (solo admin).
                        .pathMatchers(HttpMethod.DELETE, "/api/geo/**").hasRole("ADMIN")

                        .pathMatchers(HttpMethod.POST,   "/api/alertas/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/alertas/**").hasRole("ADMIN")

                        // ── Resto — solo requiere sesión válida (USER o ADMIN) ───
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtDecoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                .build();
    }

    /**
     * Construye un ReactiveJwtDecoder usando el mismo secreto HMAC-SHA256 que
     * auth-service. No se hace ninguna llamada de red — la validación es local.
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

    /**
     * Mapea el claim "role" del JWT (un único String: "ADMIN" o "USER") a una
     * GrantedAuthority "ROLE_<role>" para que .hasRole("ADMIN") funcione.
     */
    @Bean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        return jwt -> {
            String role = jwt.getClaimAsString("role");
            List<GrantedAuthority> authorities = role != null
                    ? List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    : List.of();
            return Mono.just(new JwtAuthenticationToken(jwt, authorities));
        };
    }
}
