package com.example.reportes.dto.request;

import com.example.reportes.enums.SeverityLevel;

import java.util.UUID;

public class ReporteRequestDTO {
    private UUID userId;
    private String usuarioReportante;
    private String descripcion;
    private UUID zoneId;
    private Double longitude;
    private Double latitude;
    private SeverityLevel severity;

    
    public ReporteRequestDTO() {
    }

    public UUID getUsuarioId() {
        return userId;
    }

    public void setUsuarioId(UUID usuarioId) {
        this.userId = usuarioId;
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

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
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
