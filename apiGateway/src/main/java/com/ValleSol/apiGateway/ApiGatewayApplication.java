package com.ValleSol.apiGateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@SpringBootApplication
public class ApiGatewayApplication {

    @Value("${bff.url:http://localhost:8001}")
    private String bffUrl;

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    /**
     * CORS global — acepta cualquier origen (web, Capacitor Android/iOS, emulador AVD).
     * Necesario para que la WebView de Capacitor (origen http://localhost) pueda
     * hacer peticiones cross-origin al gateway en http://10.0.2.2:8000.
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }

    /**
     * ÚNICA PUERTA DE ENTRADA.
     * Todo el tráfico del frontend pasa por el API Gateway hacia el BFF.
     * El BFF orquesta las llamadas a los microservicios.
     *
     * Frontend (:8100) → API Gateway (:8000) → BFF (:8001) → Microservicios
     */
    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // 1. Autenticación → BFF → Auth Service
                .route("bff-auth", r -> r
                        .path("/auth/**")
                        .uri(bffUrl))

                // 2. Reportes → BFF → Report Service
                .route("bff-reportes", r -> r
                        .path("/api/reportes/**")
                        .uri(bffUrl))

                // 3. Alertas → BFF → SolAlertas
                .route("bff-alertas", r -> r
                        .path("/api/alertas/**")
                        .uri(bffUrl))

                // 4. Mapa / Geo → BFF → Geo Service
                .route("bff-mapa", r -> r
                        .path("/api/mapa/**")
                        .uri(bffUrl))

                // 5. Dashboard → BFF (orquestación local)
                .route("bff-dashboard", r -> r
                        .path("/api/dashboard/**")
                        .uri(bffUrl))

                .build();
    }
}
