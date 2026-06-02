package com.ValleSol.alertservice.service;

import com.ValleSol.alertservice.dto.AlertaRequestDTO;
import com.ValleSol.alertservice.enums.Destinatarios;
import com.ValleSol.alertservice.enums.NivelEmergencia;
import com.ValleSol.alertservice.enums.OrigenAlerta;
import com.ValleSol.alertservice.enums.TipoAlerta;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class AlertaAutomaticaService {

    private final AlertaService alertaService;
    private final ClasificadorEmergencia clasificador;

    public AlertaAutomaticaService(AlertaService alertaService,
                                    ClasificadorEmergencia clasificador) {
        this.alertaService = alertaService;
        this.clasificador = clasificador;
    }

    public void generarAlertaDesdeReporte(UUID reporteId, String descripcion, String severidad) {
        NivelEmergencia nivel = mapearSeveridad(severidad);

        AlertaRequestDTO request = new AlertaRequestDTO();
        request.setTipo(TipoAlerta.AUTOMATICA.name());
        request.setMensaje("Nuevo reporte de incendio: " + descripcion);
        request.setReporteId(reporteId);
        request.setOrigenAlerta(OrigenAlerta.REPORTE.name());
        request.setNivelEmergencia(nivel.name());
        request.setDestinatarios(resolverDestinatarios(nivel).name());

        
        request.setCanalEmail(true);
        request.setCanalPush(nivel == NivelEmergencia.ALTO || nivel == NivelEmergencia.CRITICO);

        alertaService.procesarAlerta(request);
    }

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
