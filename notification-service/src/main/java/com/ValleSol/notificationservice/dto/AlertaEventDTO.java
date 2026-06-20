package com.ValleSol.notificationservice.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AlertaEventDTO {

    private UUID    reporteId;
    private String  mensaje;
    private String  tipo;

    
    private String nivelEmergencia;

  
    private String destinatarios;


    private String origenAlerta;

    private boolean canalEmail;
    private boolean canalPush;
    private UUID    despachoId;

    private String descripcionReporte;
    private String usuarioReportante;
    private Double latitude;
    private Double longitude;
    private String zoneName;
    private String zoneId;
    private LocalDateTime fechaReporte;


}
