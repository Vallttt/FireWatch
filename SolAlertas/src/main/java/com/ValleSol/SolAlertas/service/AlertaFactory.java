package com.ValleSol.SolAlertas.service;

import org.springframework.stereotype.Component;

@Component
public class AlertaFactory {


    public GeneradorAlerta obtenerGenerador(String tipo) {

        if ("SMS".equalsIgnoreCase(tipo)) {
            return new GeneradorSms();
        } else if ("EMAIL".equalsIgnoreCase(tipo)) {
            return new GeneradorEmail();
        } else {
            throw new IllegalArgumentException("Tipo de alerta no soportado: " + tipo);
        }
       
    }
}
