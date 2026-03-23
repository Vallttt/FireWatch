package com.ValleSol.SolAlertas.service;

import org.springframework.stereotype.Service;

import com.ValleSol.SolAlertas.model.Notificacion;

@Service
public class GeneradorSms implements GeneradorAlerta {

    @Override
    public Notificacion generarAlerta(String mensaje, String destinatario) {
        Notificacion alerta = new Notificacion();

        alerta.setTipoAlerta("SMS");
        alerta.setMensaje("ALERTA SMS MUNICIPALIDAD VALLE DEL SOL: " + mensaje);
        alerta.setDestinatario(destinatario);

        return alerta;
    }

}
