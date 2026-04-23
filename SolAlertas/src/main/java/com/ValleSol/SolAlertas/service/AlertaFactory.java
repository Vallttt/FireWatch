package com.ValleSol.SolAlertas.service;

import org.springframework.stereotype.Component;

/**
 * Fábrica de generadores de alerta.
 * Canal único: EMAIL para todos los tipos de alerta.
 */
@Component
public class AlertaFactory {

    public GeneradorAlerta obtenerGenerador(String tipo) {
        return new GeneradorEmail();
    }
}
