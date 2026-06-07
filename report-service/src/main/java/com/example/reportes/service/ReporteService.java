package com.example.reportes.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.reportes.dto.request.AlertRequestDTO;
import com.example.reportes.dto.request.IncidenteRequestDTO;
import com.example.reportes.dto.request.MappedReportRequestDTO;
import com.example.reportes.enums.ReportStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.reportes.dto.request.ReporteRequestDTO;
import com.example.reportes.dto.response.ReporteResponseDTO;
import com.example.reportes.model.Reporte;
import com.example.reportes.repository.ReporteRepository;

@Service
public class ReporteService {

    @Autowired
    private ReporteRepository reporteRepository;

    private final GeoService geoService;
    private final AlertService alertasService;
    private final IncidentService incidentService;
    private final EvidenceService evidenceService;

    public ReporteService(GeoService geoService, AlertService alertasService,
                          IncidentService incidentService, EvidenceService evidenceService) {
        this.geoService = geoService;
        this.alertasService = alertasService;
        this.incidentService = incidentService;
        this.evidenceService = evidenceService;
    }

    public ReporteResponseDTO crearReporte(ReporteRequestDTO request) {

        // Handle anonymous users
        if (request.getUsuarioReportante() == null || request.getUsuarioReportante().isEmpty()) {
            request.setUsuarioReportante("Anonimo");
        }

        Reporte reporte = new Reporte();
        reporte.setUsuarioId(request.getUsuarioId());
        reporte.setUsuarioReportante(request.getUsuarioReportante());
        reporte.setDescripcion(request.getDescripcion());
        reporte.setZoneId(request.getZoneId());
        reporte.setLatitude(request.getLatitude());
        reporte.setLongitude(request.getLongitude());
        reporte.setFechaIncidente(LocalDateTime.now());

        Reporte guardado = reporteRepository.save(reporte);

        // crear incidente en Incident Service (no fatal)
        try {
            IncidenteRequestDTO incidente = new IncidenteRequestDTO();
            incidente.setReporteId(guardado.getId());
            incidente.setSeverity(request.getSeverity());
            incidentService.crearIncidente(incidente);
        } catch (Exception e) {
            System.out.println("Error creando incidente: " + e.getMessage());
        }

        // Forward to Geo Service (non-fatal)
        try {
            MappedReportRequestDTO geoRequest = new MappedReportRequestDTO();
            geoRequest.setExternalReportId(guardado.getId());
            geoRequest.setReportStatus(ReportStatus.ACTIVE);
            geoRequest.setSeverity(request.getSeverity());
            geoRequest.setLatitude(request.getLatitude());
            geoRequest.setLongitude(request.getLongitude());
            geoRequest.setReportedAt(guardado.getFechaIncidente());
            geoRequest.setZoneId(request.getZoneId());
            geoService.crearMappedReport(geoRequest);
        } catch (Exception e) {
            System.out.println("Error sending report to Geo Service: " + e.getMessage());
        }

        
        // enviar a servicio de alerta (no fatal)
        try {
            AlertRequestDTO alerta = new AlertRequestDTO();
            alerta.setReporteId(guardado.getId());
            alerta.setMensaje("Nuevo reporte: " + guardado.getDescripcion());
            alerta.setTipo("EMAIL");
            alertasService.enviarAlerta(alerta);
        } catch (Exception e) {
            System.out.println("Error sending alert: " + e.getMessage());
        }

        return convertir(guardado);
    }

    public List<ReporteResponseDTO> listarReportes() {
        List<Reporte> lista = reporteRepository.findAll();
        List<ReporteResponseDTO> respuesta = new ArrayList<>();
        for (Reporte r : lista) {
            respuesta.add(convertir(r));
        }
        return respuesta;
    }

    public ReporteResponseDTO obtenerPorId(UUID id) {
        Reporte reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reporte no encontrado"));
        return convertir(reporte);
    }

    public void eliminarReporte(UUID id) {
        if (!reporteRepository.existsById(id)) {
            throw new RuntimeException("Reporte no encontrado");
        }
        // Cascade-borrar evidencias e incidente asociados (no fatal)
        try {
            evidenceService.eliminarPorReporte(id);
        } catch (Exception e) {
            System.out.println("Error eliminando evidencias: " + e.getMessage());
        }
        try {
            incidentService.eliminarPorReporte(id);
        } catch (Exception e) {
            System.out.println("Error eliminando incidente: " + e.getMessage());
        }
        reporteRepository.deleteById(id);
    }

    private ReporteResponseDTO convertir(Reporte reporte) {
        ReporteResponseDTO dto = new ReporteResponseDTO();
        dto.setId(reporte.getId());
        dto.setUsuarioId(reporte.getUsuarioId());
        dto.setUsuarioReportante(reporte.getUsuarioReportante());
        dto.setDescripcion(reporte.getDescripcion());
        dto.setFechaIncidente(reporte.getFechaIncidente());
        dto.setLatitude(reporte.getLatitude());
        dto.setLongitude(reporte.getLongitude());
        dto.setZoneId(reporte.getZoneId());
        return dto;
    }
}
