package com.ValleSol.notificationservice.dto;

import java.util.UUID;


public class AlertaEventDTO {

    private UUID    reporteId;
    private String  mensaje;
    private String  tipo;

    
    private String nivelEmergencia;

  
    private String destinatarios;


    private String origenAlerta;

    private boolean canalEmail;
    private boolean canalPush;
    private UUID    despachoId;

    public AlertaEventDTO() {}

    public UUID getReporteId()                  { return reporteId; }
    public void setReporteId(UUID reporteId)    { this.reporteId = reporteId; }

    public String getMensaje()                  { return mensaje; }
    public void setMensaje(String mensaje)      { this.mensaje = mensaje; }

    public String getTipo()                     { return tipo; }
    public void setTipo(String tipo)            { this.tipo = tipo; }

    public String getNivelEmergencia()                      { return nivelEmergencia; }
    public void setNivelEmergencia(String nivelEmergencia)  { this.nivelEmergencia = nivelEmergencia; }

    public String getDestinatarios()                    { return destinatarios; }
    public void setDestinatarios(String destinatarios)  { this.destinatarios = destinatarios; }

    public String getOrigenAlerta()                     { return origenAlerta; }
    public void setOrigenAlerta(String origenAlerta)    { this.origenAlerta = origenAlerta; }

    public boolean isCanalEmail()                   { return canalEmail; }
    public void setCanalEmail(boolean canalEmail)   { this.canalEmail = canalEmail; }

    public boolean isCanalPush()                    { return canalPush; }
    public void setCanalPush(boolean canalPush)     { this.canalPush = canalPush; }

    public UUID getDespachoId()                   { return despachoId; }
    public void setDespachoId(UUID despachoId)    { this.despachoId = despachoId; }
}
