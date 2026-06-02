package com.ValleSol.alertservice.service;

import com.ValleSol.alertservice.dto.AlertaRequestDTO;
import com.ValleSol.alertservice.enums.Destinatarios;
import com.ValleSol.alertservice.enums.NivelEmergencia;
import com.ValleSol.alertservice.enums.TipoAlerta;
import org.springframework.stereotype.Service;

/**
 * Classifies an incoming alert request into:
 *   - NivelEmergencia  (CRITICO / ALTO / MEDIO / BAJO)
 *   - Destinatarios    (who must be notified)
 *   - Channel flags    (forces push + email on high-severity alerts)
 *
 * Rules applied in priority order:
 *   1. ZONA_CRITICA             → CRITICO  | BRIGADAS_Y_ADMINISTRADORES | email+push
 *   2. INCENDIO                 → ALTO     | BRIGADAS_Y_ADMINISTRADORES | email+push
 *   3. Explicit CRITICO level   → CRITICO  | BRIGADAS_Y_ADMINISTRADORES | email+push
 *   4. Explicit ALTO level      → ALTO     | BRIGADAS_Y_ADMINISTRADORES | email+push
 *   5. SISTEMA type             → BAJO     | ADMINISTRADORES            | email only
 *   6. AUTOMATICA (no level)    → MEDIO    | ADMINISTRADORES            | email only
 *   7. MANUAL or unknown        → BAJO     | TODOS                      | email only
 */
@Service
public class ClasificadorEmergencia {

    /**
     * Computes the emergency level for the given request.
     * If the caller already specified a level it is honoured; otherwise
     * the level is derived from the alert type.
     */
    public NivelEmergencia clasificarNivel(AlertaRequestDTO request) {
        // Honour an explicitly provided level
        if (request.getNivelEmergencia() != null && !request.getNivelEmergencia().isBlank()) {
            try {
                return NivelEmergencia.valueOf(request.getNivelEmergencia().toUpperCase());
            } catch (IllegalArgumentException ignored) { /* fall through */ }
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

    /**
     * Determines the target audience based on the alert type and level.
     */
    public Destinatarios clasificarDestinatarios(AlertaRequestDTO request, NivelEmergencia nivel) {
        // Honour an explicitly provided destinatarios value
        if (request.getDestinatarios() != null && !request.getDestinatarios().isBlank()) {
            try {
                return Destinatarios.valueOf(request.getDestinatarios().toUpperCase());
            } catch (IllegalArgumentException ignored) { /* fall through */ }
        }

        return switch (nivel) {
            case CRITICO -> Destinatarios.BRIGADAS_Y_ADMINISTRADORES;
            case ALTO    -> Destinatarios.BRIGADAS_Y_ADMINISTRADORES;
            case MEDIO   -> Destinatarios.ADMINISTRADORES;
            case BAJO    -> Destinatarios.TODOS;
        };
    }

    /**
     * Forces both channels (email + push) for CRITICO and ALTO levels;
     * applies caller-provided flags for lower severity.
     */
    public void ajustarCanales(AlertaRequestDTO request, NivelEmergencia nivel) {
        if (nivel == NivelEmergencia.CRITICO || nivel == NivelEmergencia.ALTO) {
            request.setCanalEmail(true);
            request.setCanalPush(true);
        } else if (!request.isCanalEmail() && !request.isCanalPush()) {
            request.setCanalEmail(true); // default fallback
        }
    }
}
