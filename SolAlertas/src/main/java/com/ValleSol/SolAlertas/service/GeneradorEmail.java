package com.ValleSol.SolAlertas.service;

import org.springframework.stereotype.Service;

import com.ValleSol.SolAlertas.model.Notificacion;

@Service
public class GeneradorEmail implements GeneradorAlerta {

    @Override
    public Notificacion generarAlerta(String mensaje, String destinatario) {
        Notificacion alertaEmail = new Notificacion();
        
        alertaEmail.setTipoAlerta("EMAIL"); 

        // Formato simulando un asunto de correo formal
        alertaEmail.setMensaje("Asunto: [Catástrofe Municipal Valle del Sol] - " + mensaje);
        alertaEmail.setDestinatario(destinatario); 
        
        return alertaEmail;
    }

}
