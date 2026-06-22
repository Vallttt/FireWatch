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
 * Necesario para que el WebView de Capacitor (origen http://localhost) pueda realizar
 * solicitudes de origen cruzado al gateway en http://10.0.2.2:8000.
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
     * PUNTO DE ENTRADA ÚNICO.
     * Todo el tráfico del frontend pasa a través del API Gateway al BFF.
     * El BFF orquesta las llamadas a los microservicios.
     *
     * Frontend (:8100) → API Gateway (:8000) → BFF (:8001) → Microservices
     */
    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // 1. Authentication → BFF → Auth -> login
                .route("bff-auth", r -> r
                        .path("/auth/**")
                        .uri(bffUrl))

                .route("bff-api-auth", r -> r
                        .path("/api/auth/**")
                        .uri(bffUrl))
                // user -> register
                .route("bff-users",r -> r
                        .path("/api/users/**")
                        .uri(bffUrl))

                // 2. Reports → BFF → Report Service
                .route("bff-reportes", r -> r
                        .path("/api/reportes/**")
                        .uri(bffUrl))

                // 2.1 Incidents → BFF → Incident Service
                .route("bff-incidentes", r -> r
                        .path("/api/incidentes/**")
                        .uri(bffUrl))

                // 2.2 Evidence → BFF → Evidence Service
                .route("bff-evidencias", r -> r
                        .path("/api/evidencias/**")
                        .uri(bffUrl))
                        
                        // 3. Alerts → BFF → SolAlertas
                .route("bff-alertas", r -> r
                        .path("/api/alertas/**")
                        .uri(bffUrl))

                // 4. Map / Geo → BFF → Geo Service (consolidated map data)
                .route("bff-map-data", r -> r
                        .path("/api/map-data/**")
                        .uri(bffUrl))

                // 4.1 Geo → BFF → Geo Service (raw mapped reports)
                .route("bff-geo", r -> r
                        .path("/api/geo/**")
                        .uri(bffUrl))

                // 4.2 Evacuation routes → BFF → Zone Service
                .route("bff-evacuation-routes", r -> r
                        .path("/api/evacuation-routes/**")
                        .uri(bffUrl))

                // 4. Map / Geo → BFF → Zone Service
                .route("bff-zone", r -> r
                        .path("/api/zones/**")
                        .uri(bffUrl))

                // . Map / Brigades → BFF → Brigades Service
                .route("bff-brigades", r -> r
                        .path("/api/brigades/**")
                        .uri(bffUrl))

                // 5. Dashboard → BFF (local orchestration)
                .route("bff-dashboard", r -> r
                        .path("/api/dashboard/**")
                        .uri(bffUrl))


                .build();
    }
}