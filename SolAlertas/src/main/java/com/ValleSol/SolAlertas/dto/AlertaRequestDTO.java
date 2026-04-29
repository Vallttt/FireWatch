package com.ValleSol.SolAlertas.dto;

import java.util.UUID;

public class AlertaRequestDTO {

    private UUID reporteId;
    private String mensaje;
    private String tipo;
    /** Send via email channel (default true) */
    private boolean canalEmail = true;
    /** Send as push notification (default false) */
    private boolean canalPush = false;

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

    public boolean isCanalEmail() {
        return canalEmail;
    }

    public void setCanalEmail(boolean canalEmail) {
        this.canalEmail = canalEmail;
    }

    public boolean isCanalPush() {
        return canalPush;
    }

    public void setCanalPush(boolean canalPush) {
        this.canalPush = canalPush;
    }
}