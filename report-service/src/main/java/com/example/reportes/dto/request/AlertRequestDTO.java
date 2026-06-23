package com.example.reportes.dto.request;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AlertRequestDTO {

    private UUID reporteId;
    private String mensaje;
    private String tipo;
    private String protocolo;  // ✅ NUEVO: Evacuación, Incendio, Prevención, Controlado

    private String descripcionReporte;
    private String usuarioReportante;
    private Double latitude;
    private Double longitude;
    private UUID zoneId;
    private String zoneName;
    private LocalDateTime fechaReporte;
    private String nivelEmergencia;
    private String tipoIncendio; // ✅ NUEVO: FORESTAL, URBANO, ESTRUCTURAL
}