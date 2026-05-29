package com.vallesol.bff.client;

import com.vallesol.bff.dtos.request.BrigadeMapDTO;
import com.vallesol.bff.dtos.request.MappedReportMapDTO;
import com.vallesol.bff.dtos.response.ApiResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

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