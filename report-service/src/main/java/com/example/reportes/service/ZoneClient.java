package com.example.reportes.service;

import com.example.reportes.dto.response.ApiResponseDTO;
import com.example.reportes.dto.response.ZoneResponseDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class ZoneClient {

    private final RestTemplate restTemplate;

    public ZoneClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String obtenerNombreZona(UUID zoneId) {

        String url = "http://valledelsol-zone:8085/api/zones/" + zoneId;

        ResponseEntity<ApiResponseDTO<ZoneResponseDTO>> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<ApiResponseDTO<ZoneResponseDTO>>() {}
                );

        if (response.getBody() == null ||
                response.getBody().getData() == null) {
            return "Zona desconocida";
        }

        return response.getBody().getData().getName();
    }
}