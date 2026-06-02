package com.ValleSol.notificationservice.service;

import com.ValleSol.notificationservice.model.Notificacion;
import org.springframework.stereotype.Service;


@Service
public class GeneradorAdmin implements GeneradorAlerta {

    @Override
    public Notificacion generarAlerta(String mensaje, String destinatario) {
        Notificacion notif = new Notificacion();
        notif.setTipoAlerta("ADMIN");
        notif.setMensaje(
                "📋 [DIRECCIÓN EJECUTIVA] — Municipalidad Valle del Sol\n\n" +
                "AVISO ADMINISTRATIVO:\n" +
                mensaje + "\n\n" +
                "Acción requerida: Supervisión, coordinación de recursos y seguimiento."
        );
        notif.setDestinatario(destinatario);
        return notif;
    }
}
