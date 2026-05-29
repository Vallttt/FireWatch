package com.ValleSol.SolAlertas.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notificaciones")
@Data
@NoArgsConstructor
public class Notificacion {


    @Id
    @GeneratedValue
    private UUID id;
    @Column(name = "despacho_id")
    private UUID despachoId;
    @Column(name = "reporte_id")
    private UUID reporteId;
    private UUID usuarioId;
    private String tipoAlerta;
    private String destinatario;
    private String mensaje;
    private String estadoEnvio;
    /** canal de delivery: "EMAIL", "PUSH", or "EMAIL+PUSH" */
    private String canal;
    private LocalDateTime fechaEnvio;



}
