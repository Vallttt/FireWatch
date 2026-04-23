package com.vallesol.bff.config;

/**
 * CORS se maneja en el API Gateway (CorsWebFilter).
 * El BFF es un servicio interno — solo recibe llamadas del Gateway,
 * nunca directamente del navegador, por lo que no necesita CORS propio.
 */
public class CorsConfig {
    // Sin configuración CORS — el Gateway es el único punto de entrada externo.
}
