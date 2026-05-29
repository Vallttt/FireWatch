package com.example.reportes.service;


import com.example.reportes.dto.request.AlertRequestDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AlertService {

    private final RestTemplate restTemplate;

    @Value("${alertas.service.url}")
    private String alertasServiceUrl;

    public AlertService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void enviarAlerta(AlertRequestDTO request) {
        String url = alertasServiceUrl + "/api/alertas/enviar";
        restTemplate.postForEntity(url, request, Void.class);
    }
}