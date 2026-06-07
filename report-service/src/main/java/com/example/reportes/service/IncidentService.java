package com.example.reportes.service;

import com.example.reportes.dto.request.IncidenteRequestDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class IncidentService {

    private final RestTemplate restTemplate;

    @Value("${incident.service.url}")
    private String incidentServiceUrl;

    public IncidentService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void crearIncidente(IncidenteRequestDTO request) {
        String url = incidentServiceUrl + "/api/incidentes";
        restTemplate.postForEntity(url, request, Void.class);
    }

    public void eliminarPorReporte(UUID reporteId) {
        String url = incidentServiceUrl + "/api/incidentes/reporte/" + reporteId;
        restTemplate.delete(url);
    }
}
