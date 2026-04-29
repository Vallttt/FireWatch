package com.example.reportes.controller;

import java.util.List;
import java.util.UUID;

import com.example.reportes.dto.request.ReportStatusUpdateDTO;
import com.example.reportes.dto.response.ReporteMediaResponseDTO;
import com.example.reportes.service.ReporteMediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.reportes.dto.request.ReporteRequestDTO;
import com.example.reportes.dto.response.ReporteResponseDTO;
import com.example.reportes.service.ReporteService;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    @Autowired
    private ReporteMediaService mediaService;

    // ------------------------------------------------------------------ //
    //  Reports
    // ------------------------------------------------------------------ //

    @PostMapping
    public ReporteResponseDTO crearReporte(@RequestBody ReporteRequestDTO request) {
        return reporteService.crearReporte(request);
    }

    @GetMapping
    public List<ReporteResponseDTO> listarReportes() {
        return reporteService.listarReportes();
    }

    /** Active fire count used by the BFF dashboard — must be before /{id}. */
    @GetMapping("/focos-activos")
    public int contarFocosActivos() {
        return reporteService.contarFocosActivos();
    }

    @GetMapping("/{id}")
    public ReporteResponseDTO obtenerReporte(@PathVariable UUID id) {
        return reporteService.obtenerPorId(id);
    }

    @PatchMapping("/{id}/estado")
    public ReporteResponseDTO actualizarEstado(
            @PathVariable UUID id,
            @RequestBody ReportStatusUpdateDTO request) {
        return reporteService.actualizarEstado(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarReporte(@PathVariable UUID id) {
        reporteService.eliminarReporte(id);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------ //
    //  Media (photos / videos attached to a report)
    // ------------------------------------------------------------------ //

    /**
     * Upload one or more files for a report.
     * Content-Type: multipart/form-data, field name: "files"
     */
    @PostMapping("/{id}/media")
    public List<ReporteMediaResponseDTO> subirMedia(
            @PathVariable UUID id,
            @RequestParam("files") MultipartFile[] files) {
        return mediaService.guardarMedia(id, files);
    }

    /**
     * Returns all media attached to a report as base64 data-URLs.
     */
    @GetMapping("/{id}/media")
    public List<ReporteMediaResponseDTO> obtenerMedia(@PathVariable UUID id) {
        return mediaService.obtenerMedia(id);
    }
}
