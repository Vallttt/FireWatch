package com.example.incidentes.dto.response;

import com.example.incidentes.enums.ReportStatus;
import com.example.incidentes.enums.SeverityLevel;

import java.util.UUID;

public class IncidenteResponseDTO {

    private UUID id;
    private UUID reporteId;
    private ReportStatus estado;
    private SeverityLevel severity;

    public IncidenteResponseDTO() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getReporteId() {
        return reporteId;
    }

    public void setReporteId(UUID reporteId) {
        this.reporteId = reporteId;
    }

    public ReportStatus getEstado() {
        return estado;
    }

    public void setEstado(ReportStatus estado) {
        this.estado = estado;
    }

    public SeverityLevel getSeverity() {
        return severity;
    }

    public void setSeverity(SeverityLevel severity) {
        this.severity = severity;
    }
}
