package com.vallesol.bff.client;

import com.vallesol.bff.dtos.request.BrigadeRequestDTO;
import com.vallesol.bff.dtos.response.BrigadeResponseDTO;
import com.vallesol.bff.dtos.response.ApiResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BrigadeClient {

    @Value("${services.brigade.url}")
    private String brigadeServiceUrl;

    private final RestClient restClient;

    public List<BrigadeResponseDTO> getAllBrigades() {

        ApiResponseDTO<List<BrigadeResponseDTO>> response =
                restClient.get()
                    .uri(brigadeServiceUrl + "/api/brigades")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

        return response.getData();
    }

    public BrigadeResponseDTO createBrigade(BrigadeRequestDTO requestDTO){

        ApiResponseDTO<BrigadeResponseDTO> responseDTO =
                restClient.post()
                        .uri(brigadeServiceUrl + "/api/brigades")
                        .body(requestDTO)
                        .retrieve()
                        .body(new ParameterizedTypeReference<>() {});

        return responseDTO.getData();
    }

    public BrigadeResponseDTO getById(UUID id){

        ApiResponseDTO<BrigadeResponseDTO> responseDTO =
                restClient.get()
                        .uri(brigadeServiceUrl + "/api/brigades/" + id)
                        .retrieve()
                        .body(new ParameterizedTypeReference<>() {});

        return responseDTO.getData();
    }

    public BrigadeResponseDTO updateBrigade(UUID id, BrigadeRequestDTO requestDTO){

        ApiResponseDTO<BrigadeResponseDTO> responseDTO =
                restClient.put()
                        .uri(brigadeServiceUrl + "/api/brigades/" + id)
                        .body(requestDTO)
                        .retrieve()
                        .body(new ParameterizedTypeReference<>() {});

        return responseDTO.getData();
    }

    public void deleteBrigade(UUID id){
        restClient.delete()
                .uri(brigadeServiceUrl + "/api/brigades/" + id)
                .retrieve()
                .toBodilessEntity();
    }
}
