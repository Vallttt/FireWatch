package com.example.incidentes.dto.request;

import com.example.incidentes.enums.ReportStatus;

public class ReportStatusUpdateDTO {
    private ReportStatus estado;

    public ReportStatus getEstado() {
        return estado;
    }

    public void setEstado(ReportStatus estado) {
        this.estado = estado;
    }
}
