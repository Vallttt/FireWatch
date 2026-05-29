package com.vallesol.bff.config;

/**
 * CORS es manejado por el API Gateway (bean CorsWebFilter).
 * El BFF es un servicio interno — solo recibe llamadas desde el Gateway,
 * nunca directamente desde el navegador, por lo que no necesita su propia configuración de CORS.
 */
public class CorsConfig {
    // Sin configuración de CORS — el Gateway es el único punto de entrada externo.
}
