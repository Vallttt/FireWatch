package com.ValleSol.SolAlertas.service;

import org.springframework.stereotype.Component;


@Component
public class AlertaFactory {

    public GeneradorAlerta obtenerGenerador(String tipo) {

        if ("SMS".equalsIgnoreCase(tipo)) {
            return new GeneradorSms();
        } else {
            // Cualquier otro tipo (EMAIL, Evacuación, Emergencia, Preventiva, etc.)
            // se envía como EMAIL por defecto
            return new GeneradorEmail();
        }
    }
}
