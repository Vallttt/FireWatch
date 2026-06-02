package com.ValleSol.alertservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class AlertaResponseDTO {

    private UUID          id;
    private String        tipo;
    private String        mensaje;
    private UUID          reporteId;
    private UUID          despachoId;
    private String        nivelEmergencia;
    private String        destinatarios;
    private String        origenAlerta;
    private boolean       canalEmail;
    private boolean       canalPush;
    private String        estado;
    private LocalDateTime fechaCreacion;

    public UUID getId()                           { return id; }
    public void setId(UUID id)                    { this.id = id; }

    public String getTipo()                       { return tipo; }
    public void setTipo(String tipo)              { this.tipo = tipo; }

    public String getMensaje()                    { return mensaje; }
    public void setMensaje(String mensaje)        { this.mensaje = mensaje; }

    public UUID getReporteId()                    { return reporteId; }
    public void setReporteId(UUID reporteId)      { this.reporteId = reporteId; }

    public UUID getDespachoId()                   { return despachoId; }
    public void setDespachoId(UUID despachoId)    { this.despachoId = despachoId; }

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

    public String getEstado()                     { return estado; }
    public void setEstado(String estado)          { this.estado = estado; }

    public LocalDateTime getFechaCreacion()                       { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion)     { this.fechaCreacion = fechaCreacion; }
}
