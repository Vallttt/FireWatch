package com.vallesol.bff.config;

/**
 * CORS is handled by the API Gateway (CorsWebFilter bean).
 * The BFF is an internal service — it only receives calls from the Gateway,
 * never directly from the browser, so it does not need its own CORS configuration.
 */
public class CorsConfig {
    // No CORS configuration — the Gateway is the only external entry point.
}
