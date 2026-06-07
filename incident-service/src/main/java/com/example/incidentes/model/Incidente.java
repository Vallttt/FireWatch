package com.example.incidentes.model;

import java.util.UUID;

import com.example.incidentes.enums.ReportStatus;
import com.example.incidentes.enums.SeverityLevel;
import jakarta.persistence.*;

@Entity
public class Incidente {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID reporteId;

    @Enumerated(EnumType.STRING) // Para almacenar el estado como texto en la base de datos
    private ReportStatus estado;

    private SeverityLevel severity;

    public Incidente() {
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
