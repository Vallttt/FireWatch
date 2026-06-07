package com.example.reportes.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class EvidenceService {

    private final RestTemplate restTemplate;

    @Value("${evidence.service.url}")
    private String evidenceServiceUrl;

    public EvidenceService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void eliminarPorReporte(UUID reporteId) {
        String url = evidenceServiceUrl + "/api/evidencias/" + reporteId;
        restTemplate.delete(url);
    }
}
