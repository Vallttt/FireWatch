package com.ValleSol.notificationservice.service;

import com.ValleSol.notificationservice.model.Notificacion;
import org.springframework.stereotype.Service;


@Service
public class GeneradorPush implements GeneradorAlerta {

    @Override
    public Notificacion generarAlerta(String mensaje, String destinatario) {
        Notificacion notif = new Notificacion();
        notif.setTipoAlerta("PUSH");
        notif.setMensaje("[🔔 ALERTA] " + mensaje);
        notif.setDestinatario(destinatario);
        return notif;
    }
}
