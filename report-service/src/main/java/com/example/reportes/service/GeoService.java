package com.example.reportes.service;

import com.example.reportes.dto.request.MappedReportRequestDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GeoService {

    private final RestTemplate restTemplate;

    @Value("${geo.service.url}")
    private String geoServiceUrl;

    public GeoService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void crearMappedReport(MappedReportRequestDTO request) {
        String url = geoServiceUrl + "/api/mapped-reports";
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
    }
}
