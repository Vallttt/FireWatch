package com.ValleSol.alertservice.service;

import com.ValleSol.alertservice.dto.AlertaRequestDTO;
import com.ValleSol.alertservice.enums.Destinatarios;
import com.ValleSol.alertservice.enums.NivelEmergencia;
import com.ValleSol.alertservice.enums.OrigenAlerta;
import com.ValleSol.alertservice.enums.TipoAlerta;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class AlertaCriticaService {

    private final AlertaService alertaService;

    public AlertaCriticaService(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    public void emitirAlertaZonaCritica(String descripcion, UUID reporteId) {
        AlertaRequestDTO request = buildCriticalRequest(
                TipoAlerta.ZONA_CRITICA,
                "⚠️ ZONA CRÍTICA — " + descripcion,
                reporteId
        );
        alertaService.procesarAlerta(request);
    }

    public void emitirEmergenciaMaxima(String descripcion, UUID reporteId) {
        AlertaRequestDTO request = buildCriticalRequest(
                TipoAlerta.INCENDIO,
                "🚨 EMERGENCIA MÁXIMA — " + descripcion,
                reporteId
        );
        alertaService.procesarAlerta(request);
    }

  

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
