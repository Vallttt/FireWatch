package com.ValleSol.alertservice.service;

import com.ValleSol.alertservice.dto.AlertaRequestDTO;
import com.ValleSol.alertservice.dto.AlertaResponseDTO;
import com.ValleSol.alertservice.enums.Destinatarios;
import com.ValleSol.alertservice.enums.NivelEmergencia;
import com.ValleSol.alertservice.model.Alerta;
import com.ValleSol.alertservice.repository.AlertaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AlertaService {

    private final AlertaRepository         alertaRepository;
    private final NotificacionServiceClient notificacionServiceClient;
    private final ClasificadorEmergencia   clasificador;

    public AlertaService(AlertaRepository alertaRepository,
                         NotificacionServiceClient notificacionServiceClient,
                         ClasificadorEmergencia clasificador) {
        this.alertaRepository          = alertaRepository;
        this.notificacionServiceClient = notificacionServiceClient;
        this.clasificador              = clasificador;
    }

    /**
     * Core alert pipeline:
     *
     *  1. Classify emergency level (auto or from request).
     *  2. Determine target audience (auto or from request).
     *  3. Adjust channel flags (CRITICO/ALTO forces both channels).
     *  4. Assign a shared despachoId.
     *  5. Persist the Alerta record in alert_db.
     *  6. Forward the enriched event to notification-service via Eureka.
     */
    public void procesarAlerta(AlertaRequestDTO request) {
        // ── 1-3: classification & channel normalisation ──────────────────
        NivelEmergencia nivel       = clasificador.clasificarNivel(request);
        Destinatarios   destinatarios = clasificador.clasificarDestinatarios(request, nivel);
        clasificador.ajustarCanales(request, nivel);

        // ── 4: shared dispatch ID ────────────────────────────────────────
        UUID despachoId = UUID.randomUUID();

        // ── 5: persist alert ─────────────────────────────────────────────
        Alerta alerta = new Alerta();
        alerta.setTipo(request.getTipo() != null ? request.getTipo().toUpperCase() : "MANUAL");
        alerta.setMensaje(request.getMensaje());
        alerta.setReporteId(request.getReporteId());
        alerta.setDespachoId(despachoId);
        alerta.setNivelEmergencia(nivel.name());
        alerta.setDestinatarios(destinatarios.name());
        alerta.setOrigenAlerta(request.getOrigenAlerta() != null ? request.getOrigenAlerta() : "MANUAL");
        alerta.setCanalEmail(request.isCanalEmail());
        alerta.setCanalPush(request.isCanalPush());
        alerta.setEstado("PROCESADO");
        alerta.setFechaCreacion(LocalDateTime.now());
        alertaRepository.save(alerta);

        // Write classification back into request so the client receives enriched data
        request.setNivelEmergencia(nivel.name());
        request.setDestinatarios(destinatarios.name());

        // ── 6: delegate notification fan-out to notification-service ─────
        notificacionServiceClient.enviarNotificaciones(request, despachoId);
    }

    public List<AlertaResponseDTO> listarAlertas() {
        return alertaRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public long contarAlertas() {
        return alertaRepository.count();
    }

    public List<AlertaResponseDTO> listarPorNivel(String nivel) {
        return alertaRepository.findAll().stream()
                .filter(a -> nivel.equalsIgnoreCase(a.getNivelEmergencia()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ─── helpers ────────────────────────────────────────────────────────────

    private AlertaResponseDTO toDTO(Alerta a) {
        AlertaResponseDTO dto = new AlertaResponseDTO();
        dto.setId(a.getId());
        dto.setTipo(a.getTipo());
        dto.setMensaje(a.getMensaje());
        dto.setReporteId(a.getReporteId());
        dto.setDespachoId(a.getDespachoId());
        dto.setNivelEmergencia(a.getNivelEmergencia());
        dto.setDestinatarios(a.getDestinatarios());
        dto.setOrigenAlerta(a.getOrigenAlerta());
        dto.setCanalEmail(a.isCanalEmail());
        dto.setCanalPush(a.isCanalPush());
        dto.setEstado(a.getEstado());
        dto.setFechaCreacion(a.getFechaCreacion());
        return dto;
    }
}
