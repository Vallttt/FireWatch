package com.ValleSol.notificationservice.service;

import com.ValleSol.notificationservice.model.Notificacion;
import org.springframework.stereotype.Service;


@Service
public class GeneradorBrigada implements GeneradorAlerta {

    @Override
    public Notificacion generarAlerta(String mensaje, String destinatario) {
        Notificacion notif = new Notificacion();
        notif.setTipoAlerta("BRIGADA");
        notif.setMensaje(
                "🚒 BRIGADAS — DESPACHO INMEDIATO\n" +
                "Municipalidad Valle del Sol\n\n" +
                mensaje + "\n\n" +
                "Activar protocolo de emergencia. Reportar posición."
        );
        notif.setDestinatario(destinatario);
        return notif;
    }
}
