package com.example.reportes.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.reportes.dto.request.ReporteRequestDTO;
import com.example.reportes.dto.response.ReporteResponseDTO;
import com.example.reportes.service.ReporteService;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    @PostMapping
    public ReporteResponseDTO crearReporte(@RequestBody ReporteRequestDTO request) {
        return reporteService.crearReporte(request);
    }

    @GetMapping
    public List<ReporteResponseDTO> listarReportes() {
        return reporteService.listarReportes();
    }

    @GetMapping("/{id}")
    public ReporteResponseDTO obtenerReporte(@PathVariable UUID id) {
        return reporteService.obtenerPorId(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarReporte(@PathVariable UUID id) {
        reporteService.eliminarReporte(id);
        return ResponseEntity.noContent().build();
    }
}
