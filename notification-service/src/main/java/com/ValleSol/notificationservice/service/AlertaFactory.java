package com.ValleSol.notificationservice.service;

import org.springframework.stereotype.Component;


@Component
public class AlertaFactory {

    private final GeneradorEmail   generadorEmail;
    private final GeneradorPush    generadorPush;
    private final GeneradorBrigada generadorBrigada;
    private final GeneradorAdmin   generadorAdmin;

    public AlertaFactory(GeneradorEmail generadorEmail,
                        GeneradorPush generadorPush,
                        GeneradorBrigada generadorBrigada,
                        GeneradorAdmin generadorAdmin) {
        this.generadorEmail   = generadorEmail;
        this.generadorPush    = generadorPush;
        this.generadorBrigada = generadorBrigada;
        this.generadorAdmin   = generadorAdmin;
    }

    public GeneradorAlerta getGenerator(String tipo) {
        if (tipo == null) return generadorEmail;
        return switch (tipo.toUpperCase()) {
            case "PUSH"    -> generadorPush;
            case "BRIGADA" -> generadorBrigada;
            case "ADMIN"   -> generadorAdmin;
            default        -> generadorEmail;
        };
    }
}
