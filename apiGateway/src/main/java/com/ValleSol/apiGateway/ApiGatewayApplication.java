package com.ValleSol.apiGateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    // El Bean RouteLocator es el que se encarga de configurar las rutas del API Gateway:
    @Bean
    public RouteLocator interceptorAlertas(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("ms-alertas", r -> r
                        .path("/api/alertas/**")
                        .uri("http://solalertas-app:8080"))
                .build();
    }

    

}
