package com.ValleSol.alertservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents an alert decision — the fact that a situation was recognized
 * and an alert was dispatched.
 *
 * One Alerta → N Notificaciones (created by notification-service, one per user).
 * The despachoId is the traceability link between both records.
 */
@Entity
@Table(name = "alertas")
@Data
@NoArgsConstructor
public class Alerta {

    @Id
    @GeneratedValue
    private UUID id;

    /** Alert type — TipoAlerta enum value stored as String */
    private String tipo;

    @Column(columnDefinition = "TEXT")
    private String mensaje;

    @Column(name = "reporte_id")
    private UUID reporteId;

    /** Shared UUID that links this Alerta to its Notificacion records in notification-service */
    @Column(name = "despacho_id")
    private UUID despachoId;

    /** NivelEmergencia enum value: CRITICO / ALTO / MEDIO / BAJO */
    @Column(name = "nivel_emergencia")
    private String nivelEmergencia;

    /** Destinatarios enum value: TODOS / BRIGADAS / ADMINISTRADORES / BRIGADAS_Y_ADMINISTRADORES */
    private String destinatarios;

    /** OrigenAlerta enum value: REPORTE / SISTEMA / MANUAL */
    @Column(name = "origen_alerta")
    private String origenAlerta;

    @Column(name = "canal_email")
    private boolean canalEmail;

    @Column(name = "canal_push")
    private boolean canalPush;

    /** "PROCESADO" | "FALLIDO" */
    private String estado;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
}
