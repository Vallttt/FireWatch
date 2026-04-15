package com.example.reportes.controller;

import java.util.List;
import java.util.UUID;

import com.example.reportes.dto.request.ReportStatusUpdateDTO;
import com.example.reportes.enums.ReportStatus;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PatchMapping("/{id}/estado")
    public ReporteResponseDTO actualizarEstado(@PathVariable UUID id, @RequestBody ReportStatusUpdateDTO request) {
        return reporteService.actualizarEstado(id, request);
    }

    /**
     * Cuenta los reportes con estado ACTIVE (focos activos).
     * Usado por el BFF para el dashboard.
     */
    @GetMapping("/focos-activos")
    public int contarFocosActivos() {
        return reporteService.contarFocosActivos();
    }

}
