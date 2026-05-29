package com.example.reportes.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * guarda binary media (fotos / videos) adjuntos a un reporte.
 * el contenido se guarda como LONGBLOB en MySQL.
 */
@Entity
@Table(name = "reporte_media")
public class ReporteMedia {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "reporte_id", nullable = false)
    private UUID reporteId;

    @Column(nullable = false)
    private String filename;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGBLOB")
    private byte[] datos;

    @Column(name = "fecha_subida")
    private LocalDateTime fechaSubida;

    public ReporteMedia() {}

    // ------------------------------------------------------------------ //
    //  Getters / Setters
    // ------------------------------------------------------------------ //

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getReporteId() { return reporteId; }
    public void setReporteId(UUID reporteId) { this.reporteId = reporteId; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public byte[] getDatos() { return datos; }
    public void setDatos(byte[] datos) { this.datos = datos; }

    public LocalDateTime getFechaSubida() { return fechaSubida; }
    public void setFechaSubida(LocalDateTime fechaSubida) { this.fechaSubida = fechaSubida; }
}
