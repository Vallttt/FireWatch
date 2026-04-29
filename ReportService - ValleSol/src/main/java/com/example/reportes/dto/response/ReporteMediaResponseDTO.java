package com.example.reportes.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Carries a single media attachment (photo / video) as a base64 data-URL
 * so the frontend can render it directly without an extra request.
 */
public class ReporteMediaResponseDTO {

    private UUID id;
    private String filename;
    private String contentType;
    /** Base64-encoded file content (e.g. "data:image/jpeg;base64,/9j/4AAQ...") */
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
