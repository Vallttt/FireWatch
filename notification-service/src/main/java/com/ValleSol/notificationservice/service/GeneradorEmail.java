package com.ValleSol.notificationservice.service;

import com.ValleSol.notificationservice.dto.AlertaEventDTO;
import com.ValleSol.notificationservice.model.Notificacion;
import org.springframework.stereotype.Service;

@Service
public class GeneradorEmail implements GeneradorAlerta {

    @Override
    public Notificacion generarAlerta(
            AlertaEventDTO event,
            String destinatario) {

        Notificacion notif = new Notificacion();

        notif.setTipoAlerta("EMAIL");
        notif.setDestinatario(destinatario);

        notif.setMensaje(event.getMensaje());

        notif.setReporteId(event.getReporteId());
        notif.setNivelEmergencia(event.getNivelEmergencia());

        notif.setDescripcionReporte(event.getDescripcionReporte());
        notif.setUsuarioReportante(event.getUsuarioReportante());

        notif.setLatitude(event.getLatitude());
        notif.setLongitude(event.getLongitude());

        notif.setZoneName(event.getZoneName());
        notif.setFechaReporte(event.getFechaReporte());

        return notif;
    }
}