package com.example.reportes.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    // RestTemplate normal: lo usan AlertService y GeoService (URLs fijas con localhost).
    // NO se le pone @LoadBalanced para no romper esas llamadas.
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // RestTemplate con balanceo via Eureka: lo usan IncidentService y EvidenceService
    // para resolver nombres logicos (http://incident-service, http://evidence-service).
    @Bean
    @LoadBalanced
    public RestTemplate eurekaRestTemplate() {
        return new RestTemplate();
    }
}