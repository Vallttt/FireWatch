package com.example.evidencias.controller;

import java.util.List;
import java.util.UUID;

import com.example.evidencias.dto.response.ReporteMediaResponseDTO;
import com.example.evidencias.service.ReporteMediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/evidencias")
public class ReporteMediaController {

    @Autowired
    private ReporteMediaService mediaService;

    /**
     * Subir media (fotos/videos) para un reporte.
     * tipo de contenido: multipart/form-data, nombre: "files"
     */
    @PostMapping("/{reporteId}")
    public List<ReporteMediaResponseDTO> subirMedia(
            @PathVariable UUID reporteId,
            @RequestParam("files") MultipartFile[] files) {
        return mediaService.guardarMedia(reporteId, files);
    }

    /**
     * Devolver todos los medios adjuntos a un reporte como URLs de datos base64.
     */
    @GetMapping("/{reporteId}")
    public List<ReporteMediaResponseDTO> obtenerMedia(@PathVariable UUID reporteId) {
        return mediaService.obtenerMedia(reporteId);
    }

    /** Contar medios adjuntos a un reporte. */
    @GetMapping("/{reporteId}/count")
    public int contarMedia(@PathVariable UUID reporteId) {
        return mediaService.contarMedia(reporteId);
    }

    @DeleteMapping("/{reporteId}")
    public ResponseEntity<Void> eliminarMedia(@PathVariable UUID reporteId) {
        mediaService.eliminarMediaDeReporte(reporteId);
        return ResponseEntity.noContent().build();
    }
}
