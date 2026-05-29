package com.ValleSol.SolAlertas.service;

import org.springframework.stereotype.Service;

import com.ValleSol.SolAlertas.model.Notificacion;

@Service
public class GeneradorEmail implements GeneradorAlerta {

    @Override
    public Notificacion generarAlerta(String mensaje, String destinatario) {
        Notificacion emailAlert = new Notificacion();

        emailAlert.setTipoAlerta("EMAIL");

        // formato simulado de mensaje de correo electrónico: "Asunto: [Municipal Valle del Sol] - {mensaje}"
        emailAlert.setMensaje("Asunto: [Municipalidad Valle del Sol] - " + mensaje);
        emailAlert.setDestinatario(destinatario);

        return emailAlert;
    }

}
