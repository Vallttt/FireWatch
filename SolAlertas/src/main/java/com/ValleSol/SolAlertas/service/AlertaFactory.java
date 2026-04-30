package com.ValleSol.SolAlertas.service;

import org.springframework.stereotype.Component;

/**
 * Fábrica de generadores de alertas.
 * Canal único: EMAIL para todos los tipos de alertas.
 */
@Component
public class AlertaFactory {

    public GeneradorAlerta getGenerator(String tipo) {
        return new GeneradorEmail();
    }
}
