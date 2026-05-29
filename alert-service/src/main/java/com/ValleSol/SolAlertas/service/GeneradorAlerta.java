package com.ValleSol.SolAlertas.service;

import com.ValleSol.SolAlertas.model.Notificacion;

public interface GeneradorAlerta { Notificacion generarAlerta(String mensaje, String destinatario);

}
