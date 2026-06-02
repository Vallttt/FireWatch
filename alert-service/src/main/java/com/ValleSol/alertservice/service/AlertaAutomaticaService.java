package com.ValleSol.alertservice.service;

import com.ValleSol.alertservice.dto.AlertaRequestDTO;
import com.ValleSol.alertservice.enums.Destinatarios;
import com.ValleSol.alertservice.enums.NivelEmergencia;
import com.ValleSol.alertservice.enums.OrigenAlerta;
import com.ValleSol.alertservice.enums.TipoAlerta;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Generates automatic alerts that are triggered by the system without
 * any direct operator action, typically when report-service creates a
 * new fire report or the system detects a threshold condition.
 *
 * Severity mapping (from ReportService's severity field):
 *   HIGH   → NivelEmergencia.ALTO    → BRIGADAS_Y_ADMINISTRADORES | email+push
 *   MEDIUM → NivelEmergencia.MEDIO   → ADMINISTRADORES             | email
 *   LOW    → NivelEmergencia.BAJO    → TODOS                       | email
 *   (unknown) → MEDIO by default
 */
@Service
public class AlertaAutomaticaService {

    private final AlertaService alertaService;
    private final ClasificadorEmergencia clasificador;

    public AlertaAutomaticaService(AlertaService alertaService,
                                   ClasificadorEmergencia clasificador) {
        this.alertaService = alertaService;
        this.clasificador = clasificador;
    }

    /**
     * Auto-alert triggered by a new report from report-service.
     *
     * @param reporteId   the ID of the originating report
     * @param descripcion report description (used as alert message body)
     * @param severidad   severity string from report-service: "HIGH", "MEDIUM", "LOW"
     */
    public void generarAlertaDesdeReporte(UUID reporteId, String descripcion, String severidad) {
        NivelEmergencia nivel = mapearSeveridad(severidad);

        AlertaRequestDTO request = new AlertaRequestDTO();
        request.setTipo(TipoAlerta.AUTOMATICA.name());
        request.setMensaje("Nuevo reporte de incendio: " + descripcion);
        request.setReporteId(reporteId);
        request.setOrigenAlerta(OrigenAlerta.REPORTE.name());
        request.setNivelEmergencia(nivel.name());
        request.setDestinatarios(resolverDestinatarios(nivel).name());

        // Channel assignment based on severity
        request.setCanalEmail(true);
        request.setCanalPush(nivel == NivelEmergencia.ALTO || nivel == NivelEmergencia.CRITICO);

        alertaService.procesarAlerta(request);
    }

    /**
     * System-generated alert (e.g. scheduled threshold check, daily summary).
     *
     * @param mensaje system-generated message body
     */
    public void generarAlertaDeSistema(String mensaje) {
        AlertaRequestDTO request = new AlertaRequestDTO();
        request.setTipo(TipoAlerta.SISTEMA.name());
        request.setMensaje("[Sistema Valle del Sol] " + mensaje);
        request.setOrigenAlerta(OrigenAlerta.SISTEMA.name());
        request.setNivelEmergencia(NivelEmergencia.BAJO.name());
        request.setDestinatarios(Destinatarios.ADMINISTRADORES.name());
        request.setCanalEmail(true);
        request.setCanalPush(false);

        alertaService.procesarAlerta(request);
    }

    // ─── helpers ────────────────────────────────────────────────────────────

    private NivelEmergencia mapearSeveridad(String severidad) {
        if (severidad == null) return NivelEmergencia.MEDIO;
        return switch (severidad.toUpperCase()) {
            case "HIGH",   "ALTA",   "CRITICO" -> NivelEmergencia.ALTO;
            case "MEDIUM", "MEDIA",  "MEDIO"   -> NivelEmergencia.MEDIO;
            case "LOW",    "BAJA",   "BAJO"    -> NivelEmergencia.BAJO;
            default -> NivelEmergencia.MEDIO;
        };
    }

    private Destinatarios resolverDestinatarios(NivelEmergencia nivel) {
        return switch (nivel) {
            case CRITICO, ALTO -> Destinatarios.BRIGADAS_Y_ADMINISTRADORES;
            case MEDIO         -> Destinatarios.ADMINISTRADORES;
            case BAJO          -> Destinatarios.TODOS;
        };
    }
}
