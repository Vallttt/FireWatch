package com.example.incidentes.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.incidentes.dto.request.IncidenteRequestDTO;
import com.example.incidentes.dto.request.ReportStatusUpdateDTO;
import com.example.incidentes.enums.ReportStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.example.incidentes.dto.response.IncidenteResponseDTO;
import com.example.incidentes.model.Incidente;
import com.example.incidentes.repository.IncidenteRepository;

@Service
public class IncidenteService {

    @Autowired
    private IncidenteRepository incidenteRepository;

    @Autowired
    private RestTemplate restTemplate;

    private static final String ZONE_SERVICE_URL = "http://zone-app:8085";

    public IncidenteResponseDTO crearIncidente(IncidenteRequestDTO request) {
        Incidente incidente = new Incidente();
        incidente.setReporteId(request.getReporteId());
        incidente.setSeverity(request.getSeverity());
        incidente.setEstado(ReportStatus.ACTIVE);

        Incidente guardado = incidenteRepository.save(incidente);
        return convertir(guardado);
    }

    public List<IncidenteResponseDTO> listarIncidentes() {
        List<Incidente> lista = incidenteRepository.findAll();
        List<IncidenteResponseDTO> respuesta = new ArrayList<>();
        for (Incidente i : lista) {
            respuesta.add(convertir(i));
        }
        return respuesta;
    }

    public IncidenteResponseDTO obtenerPorId(UUID id) {
        Incidente incidente = incidenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incidente no encontrado"));
        return convertir(incidente);
    }

    public IncidenteResponseDTO obtenerPorReporteId(UUID reporteId) {
        List<Incidente> lista = incidenteRepository.findByReporteId(reporteId);
        if (lista.isEmpty()) {
            throw new RuntimeException("Incidente no encontrado");
        }
        return convertir(lista.get(0));
    }

    public IncidenteResponseDTO actualizarEstado(UUID id, ReportStatusUpdateDTO request) {
        Incidente incidente = incidenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incidente no encontrado"));

        // Si el incendio cambia a INACTIVE, eliminar las rutas de evacuación
        if (request.getEstado() == ReportStatus.INACTIVE && incidente.getEstado() == ReportStatus.ACTIVE) {
            try {
                String url = ZONE_SERVICE_URL + "/api/evacuation-routes/report/" + incidente.getReporteId();
                restTemplate.delete(url);
                System.out.println("Rutas de evacuación eliminadas para reporte: " + incidente.getReporteId());
            } catch (Exception e) {
                System.err.println("Error eliminando rutas de evacuación: " + e.getMessage());
            }
        }

        incidente.setEstado(request.getEstado());
        return convertir(incidenteRepository.save(incidente));
    }

    @Transactional
    public void eliminarPorReporteId(UUID reporteId) {
        incidenteRepository.deleteByReporteId(reporteId);
    }

    public int contarFocosActivos() {
        return (int) incidenteRepository.findAll().stream()
                .filter(i -> i.getEstado() == ReportStatus.ACTIVE)
                .count();
    }

    private IncidenteResponseDTO convertir(Incidente incidente) {
        IncidenteResponseDTO dto = new IncidenteResponseDTO();
        dto.setId(incidente.getId());
        dto.setReporteId(incidente.getReporteId());
        dto.setEstado(incidente.getEstado());
        dto.setSeverity(incidente.getSeverity());
        return dto;
    }
}
