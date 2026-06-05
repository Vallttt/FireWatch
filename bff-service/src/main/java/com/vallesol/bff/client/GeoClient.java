package com.vallesol.bff.client;

import com.vallesol.bff.dtos.request.MappedReportMapDTO;
import com.vallesol.bff.dtos.response.ApiResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GeoClient {

    @Value("${services.geo.url}")
    private String geoServiceUrl;

    private final RestClient restClient;

    public List<MappedReportMapDTO> findAllMappedReports() {

        ApiResponseDTO<List<MappedReportMapDTO>> response =
                restClient.get()
                .uri(geoServiceUrl + "/api/geo")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        return response.getData();
    }
}