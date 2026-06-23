package com.ValleSol.alertservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "alertas")
@Data
@NoArgsConstructor
public class Alerta {

    @Id
    @GeneratedValue
    private UUID id;


    private String tipo;

    @Column(columnDefinition = "TEXT")
    private String mensaje;

    @Column(name = "reporte_id")
    private UUID reporteId;


    @Column(name = "despacho_id")
    private UUID despachoId;

    @Column(name = "nivel_emergencia")
    private String nivelEmergencia;


    private String destinatarios;


    @Column(name = "origen_alerta")
    private String origenAlerta;

    @Column(name = "canal_email")
    private boolean canalEmail;

    @Column(name = "canal_push")
    private boolean canalPush;


    private String estado;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "usuario_remitente")
    private String usuarioRemitente;
}
