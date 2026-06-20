package com.ValleSol.notificationservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notificaciones")
@Data
@NoArgsConstructor
public class Notificacion {

    @Id
    @GeneratedValue
    private UUID id;

    
    private UUID despachoId;

    @Column(name = "reporte_id")
    private UUID reporteId;

    private UUID usuarioId;

   
    private String tipoAlerta;

    private String destinatario;

    @Column(columnDefinition = "TEXT")
    private String mensaje;

    private String estadoEnvio;

   
    private String canal;

    
    @Column(name = "nivel_emergencia")
    private String nivelEmergencia;

    
    @Column(name = "tipo_destinatario")
    private String tipoDestinatario;

    private LocalDateTime fechaEnvio;

    @Column(columnDefinition = "TEXT")
    private String descripcionReporte;

    private String usuarioReportante;

    private Double latitude;

    private Double longitude;

    private String zoneName;

    private String zoneId;

    private LocalDateTime fechaReporte;
}
