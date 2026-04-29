package com.ValleSol.SolAlertas.service;

import org.springframework.stereotype.Component;

/**
 * Factory for alert generators.
 * Single channel: EMAIL for all alert types.
 */
@Component
public class AlertaFactory {

    public GeneradorAlerta getGenerator(String tipo) {
        return new GeneradorEmail();
    }
}
