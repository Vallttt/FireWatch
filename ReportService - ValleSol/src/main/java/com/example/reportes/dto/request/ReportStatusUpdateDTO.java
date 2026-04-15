package com.example.reportes.dto.request;

import com.example.reportes.enums.ReportStatus;

public class ReportStatusUpdateDTO {
    private ReportStatus estado;

    public ReportStatus getEstado() {
        return estado;
    }

    public void setEstado(ReportStatus estado) {
        this.estado = estado;
    }
}
