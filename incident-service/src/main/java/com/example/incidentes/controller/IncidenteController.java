package com.example.incidentes.controller;

import java.util.List;
import java.util.UUID;

import com.example.incidentes.dto.request.IncidenteRequestDTO;
import com.example.incidentes.dto.request.ReportStatusUpdateDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.incidentes.dto.response.IncidenteResponseDTO;
import com.example.incidentes.service.IncidenteService;

@RestController
@RequestMapping("/api/incidentes")
public class IncidenteController {

    @Autowired
    private IncidenteService incidenteService;

    @PostMapping
    public IncidenteResponseDTO crearIncidente(@RequestBody IncidenteRequestDTO request) {
        return incidenteService.crearIncidente(request);
    }

    @GetMapping
    public List<IncidenteResponseDTO> listarIncidentes() {
        return incidenteService.listarIncidentes();
    }

    /** Contar focos activos. */
    @GetMapping("/focos-activos")
    public int contarFocosActivos() {
        return incidenteService.contarFocosActivos();
    }

    @GetMapping("/{id}")
    public IncidenteResponseDTO obtenerIncidente(@PathVariable UUID id) {
        return incidenteService.obtenerPorId(id);
    }

    @GetMapping("/reporte/{reporteId}")
    public IncidenteResponseDTO obtenerPorReporte(@PathVariable UUID reporteId) {
        return incidenteService.obtenerPorReporteId(reporteId);
    }

    @PatchMapping("/{id}/estado")
    public IncidenteResponseDTO actualizarEstado(
            @PathVariable UUID id,
            @RequestBody ReportStatusUpdateDTO request) {
        return incidenteService.actualizarEstado(id, request);
    }

    @DeleteMapping("/reporte/{reporteId}")
    public ResponseEntity<Void> eliminarPorReporte(@PathVariable UUID reporteId) {
        incidenteService.eliminarPorReporteId(reporteId);
        return ResponseEntity.noContent().build();
    }
}
