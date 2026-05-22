package com.example.reportes.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * lleva a single media attachment (foto / video) como base64 data-URL
 * para que el frontend lo pueda renderizar directamente sin una solicitud adicional.
 */
public class ReporteMediaResponseDTO {

    private UUID id;
    private String filename;
    private String contentType;
    /** Base64-encoded contenido del archivo (e.g. "data:image/jpeg;base64,/9j/4AAQ...") */
    private String data;
    private LocalDateTime fechaSubida;

    public ReporteMediaResponseDTO() {}

    // ------------------------------------------------------------------ //
    //  Getters / Setters
    // ------------------------------------------------------------------ //

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public LocalDateTime getFechaSubida() { return fechaSubida; }
    public void setFechaSubida(LocalDateTime fechaSubida) { this.fechaSubida = fechaSubida; }
}
