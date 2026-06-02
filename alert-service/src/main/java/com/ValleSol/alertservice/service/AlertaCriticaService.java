package com.ValleSol.alertservice.service;

import com.ValleSol.alertservice.dto.AlertaRequestDTO;
import com.ValleSol.alertservice.enums.Destinatarios;
import com.ValleSol.alertservice.enums.NivelEmergencia;
import com.ValleSol.alertservice.enums.OrigenAlerta;
import com.ValleSol.alertservice.enums.TipoAlerta;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Handles critical zone and high-priority emergency alerts.
 *
 * Critical alerts are characterized by:
 *  - NivelEmergencia.CRITICO
 *  - Both email and push channels forced active
 *  - Target: BRIGADAS_Y_ADMINISTRADORES (field teams + command)
 *
 * Typical triggers:
 *  - A geographic zone is flagged as critical (from geo-service)
 *  - An operator manually escalates an existing alert
 *  - Automatic escalation based on fire count thresholds
 */
@Service
public class AlertaCriticaService {

    private final AlertaService alertaService;

    public AlertaCriticaService(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    /**
     * Emits a critical zone alert.
     *
     * @param descripcion human-readable description of the critical situation
     * @param reporteId   optional originating report ID (may be null)
     */
    public void emitirAlertaZonaCritica(String descripcion, UUID reporteId) {
        AlertaRequestDTO request = buildCriticalRequest(
                TipoAlerta.ZONA_CRITICA,
                "⚠️ ZONA CRÍTICA — " + descripcion,
                reporteId
        );
        alertaService.procesarAlerta(request);
    }

    /**
     * Emits an emergency escalation alert (e.g., multiple concurrent fires).
     *
     * @param descripcion   situation summary
     * @param reporteId     optional originating report
     */
    public void emitirEmergenciaMaxima(String descripcion, UUID reporteId) {
        AlertaRequestDTO request = buildCriticalRequest(
                TipoAlerta.INCENDIO,
                "🚨 EMERGENCIA MÁXIMA — " + descripcion,
                reporteId
        );
        alertaService.procesarAlerta(request);
    }

    // ─── helpers ────────────────────────────────────────────────────────────

    private AlertaRequestDTO buildCriticalRequest(TipoAlerta tipo, String mensaje, UUID reporteId) {
        AlertaRequestDTO req = new AlertaRequestDTO();
        req.setTipo(tipo.name());
        req.setMensaje(mensaje);
        req.setReporteId(reporteId);
        req.setNivelEmergencia(NivelEmergencia.CRITICO.name());
        req.setDestinatarios(Destinatarios.BRIGADAS_Y_ADMINISTRADORES.name());
        req.setOrigenAlerta(OrigenAlerta.SISTEMA.name());
        req.setCanalEmail(true);
        req.setCanalPush(true);
        return req;
    }
}
