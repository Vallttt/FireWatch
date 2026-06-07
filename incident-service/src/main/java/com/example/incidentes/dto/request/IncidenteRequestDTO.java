package com.example.incidentes.dto.request;

import com.example.incidentes.enums.SeverityLevel;

import java.util.UUID;

public class IncidenteRequestDTO {
    private UUID reporteId;
    private SeverityLevel severity;

    public IncidenteRequestDTO() {
    }

    public UUID getReporteId() {
        return reporteId;
    }

    public void setReporteId(UUID reporteId) {
        this.reporteId = reporteId;
    }

    public SeverityLevel getSeverity() {
        return severity;
    }

    public void setSeverity(SeverityLevel severity) {
        this.severity = severity;
    }
}
