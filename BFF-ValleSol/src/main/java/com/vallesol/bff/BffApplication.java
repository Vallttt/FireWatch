package com.vallesol.bff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients // esto llama a los microservicios
public class BffApplication {
    public static void main(String[] args) {
        SpringApplication.run(BffApplication.class, args);
    }
}
