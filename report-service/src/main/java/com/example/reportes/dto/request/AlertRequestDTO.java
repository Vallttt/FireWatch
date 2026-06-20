package com.example.reportes.dto.request;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AlertRequestDTO {

    private UUID reporteId;
    private String mensaje;
    private String tipo;

    private String descripcionReporte;
    private String usuarioReportante;
    private Double latitude;
    private Double longitude;
    private UUID zoneId;
    private LocalDateTime fechaReporte;
    private String nivelEmergencia;
}