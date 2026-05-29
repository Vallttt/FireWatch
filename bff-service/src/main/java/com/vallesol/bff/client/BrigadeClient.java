package com.vallesol.bff.client;

import com.vallesol.bff.dtos.request.BrigadeMapDTO;
import com.vallesol.bff.dtos.response.ApiResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BrigadeClient {

    @Value("${services.brigade.url}")
    private String brigadeServiceUrl;

    private final RestClient restClient;

    public List<BrigadeMapDTO> findAllBrigades() {

        ApiResponseDTO<List<BrigadeMapDTO>> response =
                restClient.get()
                    .uri(brigadeServiceUrl + "/api/brigades")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

        return response.getData();
    }
}
