package com.ValleSol.notificationservice.service;

import com.ValleSol.notificationservice.model.Notificacion;


public interface GeneradorAlerta {
    Notificacion generarAlerta(String mensaje, String destinatario);
}
