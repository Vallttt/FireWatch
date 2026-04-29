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
     * Global CORS — accepts any origin (web, Capacitor Android/iOS, AVD emulator).
     * Required so that the Capacitor WebView (origin http://localhost) can make
     * cross-origin requests to the gateway at http://10.0.2.2:8000.
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
     * SINGLE ENTRY POINT.
     * All frontend traffic passes through the API Gateway to the BFF.
     * The BFF orchestrates calls to the microservices.
     *
     * Frontend (:8100) → API Gateway (:8000) → BFF (:8001) → Microservices
     */
    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // 1. Authentication → BFF → Auth Service
                .route("bff-auth", r -> r
                        .path("/auth/**")
                        .uri(bffUrl))

                // 2. Reports → BFF → Report Service
                .route("bff-reportes", r -> r
                        .path("/api/reportes/**")
                        .uri(bffUrl))

                // 3. Alerts → BFF → SolAlertas
                .route("bff-alertas", r -> r
                        .path("/api/alertas/**")
                        .uri(bffUrl))

                // 4. Map / Geo → BFF → Geo Service
                .route("bff-mapa", r -> r
                        .path("/api/mapa/**")
                        .uri(bffUrl))

                // 5. Dashboard → BFF (local orchestration)
                .route("bff-dashboard", r -> r
                        .path("/api/dashboard/**")
                        .uri(bffUrl))

                .build();
    }
}
