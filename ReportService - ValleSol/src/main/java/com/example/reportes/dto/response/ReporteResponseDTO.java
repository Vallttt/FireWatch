package com.example.reportes.dto.response;

import com.example.reportes.enums.ReportStatus;
import com.example.reportes.enums.SeverityLevel;

import java.time.LocalDateTime;
import java.util.UUID;

public class ReporteResponseDTO {
    
    private UUID id;
    private UUID usuarioId;
    private String usuarioReportante;
    private String descripcion;
    private ReportStatus estado;
    private LocalDateTime fechaIncidente;

    private UUID zoneId;
    private Double longitude;
    private Double latitude;
    private SeverityLevel severity;
    
    public ReporteResponseDTO() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(UUID usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioReportante() {
        return usuarioReportante;
    }

    public void setUsuarioReportante(String usuarioReportante) {
        this.usuarioReportante = usuarioReportante;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public ReportStatus getEstado() {
        return estado;
    }

    public void setEstado(ReportStatus estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaIncidente() {
        return fechaIncidente;
    }

    public void setFechaIncidente(LocalDateTime fechaIncidente) {
        this.fechaIncidente = fechaIncidente;
    }

    public UUID getZoneId() {
        return zoneId;
    }

    public void setZoneId(UUID zoneId) {
        this.zoneId = zoneId;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public SeverityLevel getSeverity() {
        return severity;
    }

    public void setSeverity(SeverityLevel severity) {
        this.severity = severity;
    }
}
