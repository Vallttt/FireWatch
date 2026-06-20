package com.ValleSol.alertservice.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AlertaRequestDTO {

    private UUID reporteId;
    private String mensaje;
    private String tipo;

    private String nivelEmergencia;
    private String destinatarios;
    private String origenAlerta;

    private boolean canalEmail = true;
    private boolean canalPush = false;

    // NUEVOS CAMPOS
    private String descripcionReporte;
    private String usuarioReportante;
    private Double latitude;
    private Double longitude;
    private UUID zoneId;
    private String zoneName;
    private LocalDateTime fechaReporte;
}
