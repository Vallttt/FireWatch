package com.ValleSol.notificationservice.service;

import com.ValleSol.notificationservice.dto.AlertaEventDTO;
import com.ValleSol.notificationservice.model.Notificacion;

public interface GeneradorAlerta {

    Notificacion generarAlerta(
            AlertaEventDTO event,
            String destinatario
    );
}