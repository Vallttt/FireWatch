package com.example.incidentes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class IncidentesApplication {

    public static void main(String[] args) {
        SpringApplication.run(IncidentesApplication.class, args);
    }

}