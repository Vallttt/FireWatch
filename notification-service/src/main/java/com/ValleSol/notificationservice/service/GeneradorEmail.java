package com.ValleSol.notificationservice.service;

import com.ValleSol.notificationservice.model.Notificacion;
import org.springframework.stereotype.Service;


@Service
public class GeneradorEmail implements GeneradorAlerta {

    @Override
    public Notificacion generarAlerta(String mensaje, String destinatario) {
        Notificacion notif = new Notificacion();
        notif.setTipoAlerta("EMAIL");
        notif.setMensaje("Asunto: [Municipalidad Valle del Sol] - " + mensaje);
        notif.setDestinatario(destinatario);
        return notif;
    }
}
