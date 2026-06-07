package com.example.reportes.model;


import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
public class Reporte {
   
    @Id
    @GeneratedValue
    private UUID id;

    private UUID userId;
    private String usuarioReportante;
    private LocalDateTime fechaIncidente;
    private String descripcion;

    private UUID zoneId;
    private Double longitude;
    private Double latitude;
    
    
    public Reporte() {
    }


    public UUID getId() {
        return id;
    }


    public void setId(UUID id) {
        this.id = id;
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


    public LocalDateTime getFechaIncidente() {
        return fechaIncidente;
    }


    public void setFechaIncidente(LocalDateTime fechaIncidente) {
        this.fechaIncidente = fechaIncidente;
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
}
