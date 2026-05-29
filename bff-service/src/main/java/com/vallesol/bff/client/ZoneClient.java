package com.vallesol.bff.client;

import com.vallesol.bff.dtos.request.ZoneMapDTO;
import com.vallesol.bff.dtos.response.ApiResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ZoneClient {

    @Value("${services.zone.url}")
    private String zoneServiceUrl;

    private final RestClient restClient;

    public List<ZoneMapDTO> findAllZones() {

        ApiResponseDTO<List<ZoneMapDTO>> response =
                restClient.get()
                        .uri(zoneServiceUrl + "/api/zones")
                        .retrieve()
                        .body(new ParameterizedTypeReference<>() {});

        return response.getData();
    }
}
