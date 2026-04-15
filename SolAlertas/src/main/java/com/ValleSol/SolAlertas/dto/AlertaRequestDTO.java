package com.ValleSol.SolAlertas.dto;

import java.util.UUID;

public class AlertaRequestDTO {

    private UUID reporteId;
    private String mensaje;
    private String tipo;

    public AlertaRequestDTO() {
    }

    public UUID getReporteId() {
        return reporteId;
    }

    public void setReporteId(UUID reporteId) {
        this.reporteId = reporteId;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}