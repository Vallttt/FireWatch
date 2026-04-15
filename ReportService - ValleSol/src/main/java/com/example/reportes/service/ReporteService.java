package com.example.reportes.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.reportes.dto.request.AlertRequestDTO;
import com.example.reportes.dto.request.MappedReportRequestDTO;
import com.example.reportes.dto.request.ReportStatusUpdateDTO;
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


    public ReporteService(GeoService geoService, AlertService alertasService) {
        this.geoService = geoService;

        this.alertasService = alertasService;
    }

    public ReporteResponseDTO crearReporte(ReporteRequestDTO request) {

        // Manejo de anonimato
        if (request.getUsuarioReportante() == null || request.getUsuarioReportante().isEmpty()) {
            request.setUsuarioReportante("Anonimo");
        }

        Reporte reporte = new Reporte();

        // Usuario (puede ser null)
        reporte.setUsuarioId(request.getUsuarioId());

        reporte.setUsuarioReportante(request.getUsuarioReportante());
        reporte.setDescripcion(request.getDescripcion());
        reporte.setZoneId(request.getZoneId());
        reporte.setLatitude(request.getLatitude());
        reporte.setLongitude(request.getLongitude());
        reporte.setSeverity(request.getSeverity());
        reporte.setFechaIncidente(LocalDateTime.now());
        reporte.setEstado(ReportStatus.ACTIVE);

        // 1. Guardar reporte
        Reporte guardado = reporteRepository.save(reporte);

        // 2. Mandar a GEO (no falla el reporte si Geo no responde)
        try {
            MappedReportRequestDTO geoRequest = new MappedReportRequestDTO();
            geoRequest.setExternalReportId(guardado.getId());
            geoRequest.setReportStatus(guardado.getEstado());
            geoRequest.setSeverity(request.getSeverity());
            geoRequest.setLatitude(request.getLatitude());
            geoRequest.setLongitude(request.getLongitude());
            geoRequest.setReportedAt(guardado.getFechaIncidente());
            geoRequest.setZoneId(request.getZoneId());

            geoService.crearMappedReport(geoRequest);
        } catch (Exception e) {
            System.out.println("Error enviando reporte a Geo Service: " + e.getMessage());
        }

        // 3. Mandar a ALERTAS 
        try {
            AlertRequestDTO alerta = new AlertRequestDTO();
            alerta.setReporteId(guardado.getId());
            alerta.setMensaje("Nuevo reporte: " + guardado.getDescripcion());
            alerta.setTipo("EMAIL"); // después puedes hacerlo dinámico

            alertasService.enviarAlerta(alerta);

        } catch (Exception e) {
            System.out.println("Error enviando alerta: " + e.getMessage());
        }

        return convertir(guardado);
    }

    public List<ReporteResponseDTO> listarReportes() {

        List<Reporte> lista = reporteRepository.findAll();
        List<ReporteResponseDTO> respuesta = new ArrayList<>();

        for (int i = 0; i < lista.size(); i++) {
            respuesta.add(convertir(lista.get(i)));
        }

        return respuesta;
    }

    public ReporteResponseDTO obtenerPorId(UUID id) {

        Reporte reporte = reporteRepository
                .findById(id).orElse(null);

        if (reporte == null) {
            throw new RuntimeException("Reporte no encontrado");
        }

        return convertir(reporte);
    }

    //cambio de update de estado por PATCH, ademas no recibe parametro sino un request
    public ReporteResponseDTO actualizarEstado(UUID id, ReportStatusUpdateDTO request) {

        Reporte reporte = reporteRepository.findById(id).orElse(null);

        if (reporte == null) {
            throw new RuntimeException("Reporte no encontrado");
        } else {
            reporte.setEstado(request.getEstado());
        }

        Reporte actualizado = reporteRepository.save(reporte);

        return convertir(actualizado);
    }

    public int contarFocosActivos() {
        return (int) reporteRepository.findAll().stream()
                .filter(r -> r.getEstado() == ReportStatus.ACTIVE)
                .count();
    }

    private ReporteResponseDTO convertir(Reporte reporte) {

        ReporteResponseDTO dto = new ReporteResponseDTO();

        dto.setId(reporte.getId());
        dto.setUsuarioId(reporte.getUsuarioId());
        dto.setUsuarioReportante(reporte.getUsuarioReportante());
        dto.setDescripcion(reporte.getDescripcion());
        dto.setEstado(reporte.getEstado());
        dto.setFechaIncidente(reporte.getFechaIncidente());
        dto.setLatitude(reporte.getLatitude());
        dto.setLongitude(reporte.getLongitude());
        dto.setSeverity(reporte.getSeverity());
        dto.setZoneId(reporte.getZoneId());

        return dto;
    }
    

        

}
