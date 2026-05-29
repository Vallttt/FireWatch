package com.vallesol.bff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableFeignClients
public class BffApplication {
    public static void main(String[] args) {
        SpringApplication.run(BffApplication.class, args);
    }

    /**
    * Usado por ReportesProxyController para reenviar cargas multipart al Servicio de Reportes.
    */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
