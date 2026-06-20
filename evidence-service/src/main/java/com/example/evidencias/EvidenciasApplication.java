package com.example.evidencias;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class EvidenciasApplication {

    public static void main(String[] args) {
        SpringApplication.run(EvidenciasApplication.class, args);
    }

}
