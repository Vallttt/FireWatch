package com.ValleSol.alertservice.service;

import com.ValleSol.alertservice.dto.AlertaRequestDTO;
import com.ValleSol.alertservice.enums.Destinatarios;
import com.ValleSol.alertservice.enums.NivelEmergencia;
import com.ValleSol.alertservice.enums.TipoAlerta;
import org.springframework.stereotype.Service;

@Service
public class ClasificadorEmergencia {

    public NivelEmergencia clasificarNivel(AlertaRequestDTO request) {
        
        if (request.getNivelEmergencia() != null && !request.getNivelEmergencia().isBlank()) {
            try {
                return NivelEmergencia.valueOf(request.getNivelEmergencia().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        String tipo = request.getTipo() != null ? request.getTipo().toUpperCase() : "";

        return switch (tipo) {
            case "ZONA_CRITICA" -> NivelEmergencia.CRITICO;
            case "INCENDIO"     -> NivelEmergencia.ALTO;
            case "SISTEMA"      -> NivelEmergencia.BAJO;
            case "AUTOMATICA"   -> NivelEmergencia.MEDIO;
            default             -> NivelEmergencia.BAJO;
        };
    }

    
    public Destinatarios clasificarDestinatarios(AlertaRequestDTO request, NivelEmergencia nivel) {

        if (request.getDestinatarios() != null && !request.getDestinatarios().isBlank()) {
            try {
                return Destinatarios.valueOf(request.getDestinatarios().toUpperCase());
            } catch (IllegalArgumentException ignored) { }
        }

        return switch (nivel) {
            case CRITICO -> Destinatarios.BRIGADAS_Y_ADMINISTRADORES;
            case ALTO    -> Destinatarios.BRIGADAS_Y_ADMINISTRADORES;
            case MEDIO   -> Destinatarios.ADMINISTRADORES;
            case BAJO    -> Destinatarios.TODOS;
        };
    }

    public void ajustarCanales(AlertaRequestDTO request, NivelEmergencia nivel) {
        if (nivel == NivelEmergencia.CRITICO || nivel == NivelEmergencia.ALTO) {
            request.setCanalEmail(true);
            request.setCanalPush(true);
        } else if (!request.isCanalEmail() && !request.isCanalPush()) {
            request.setCanalEmail(true); // default fallback
        }
    }
}
