package com.vallesol.bff.client;

import com.vallesol.bff.dtos.request.EvacuationRouteRequestDTO;
import com.vallesol.bff.dtos.response.ApiResponseDTO;
import com.vallesol.bff.dtos.response.EvacuationResponseDTO;
import com.vallesol.bff.dtos.response.ZoneResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EvacuationRoutesClient {

    private final RestClient restClient;

    @Value("${services.zone.url}")
    private String evacroutesServiceUrl;

    public List<EvacuationResponseDTO> getAllRoutes() {

        ApiResponseDTO<List<EvacuationResponseDTO>> response =
                restClient.get()
                        .uri(evacroutesServiceUrl + "/api/evacuation-routes")
                        .retrieve()
                        .body(new ParameterizedTypeReference<>() {});

        return response.getData();
    }

    public EvacuationResponseDTO createRoute(EvacuationRouteRequestDTO requestDTO){

        ApiResponseDTO<EvacuationResponseDTO> responseDTO =
                restClient.post()
                        .uri(evacroutesServiceUrl + "/api/evacuation-routes")
                        .body(requestDTO)
                        .retrieve()
                        .body(new ParameterizedTypeReference<>() {});

        return responseDTO.getData();
    }

    public EvacuationResponseDTO getRouteById(UUID id){

        ApiResponseDTO<EvacuationResponseDTO> responseDTO =
                restClient.get()
                        .uri(evacroutesServiceUrl + "/api/evacuation-route/" + id)
                        .retrieve()
                        .body(new ParameterizedTypeReference<>() {});

        return responseDTO.getData();
    }

    public List<EvacuationResponseDTO> getRoutesByZone(UUID id){

        ApiResponseDTO<List<EvacuationResponseDTO>> responseDTO =
                restClient.get()
                        .uri(evacroutesServiceUrl + "/api/evacuation-routes/zone/" + id)
                        .retrieve()
                        .body(new ParameterizedTypeReference<>() {});

        return responseDTO.getData();
    }

    public EvacuationResponseDTO updateRoute(UUID id, EvacuationRouteRequestDTO requestDTO){

        ApiResponseDTO<EvacuationResponseDTO> responseDTO =
                restClient.put()
                        .uri(evacroutesServiceUrl + "/api/evacuation-route/" + id)
                        .body(requestDTO)
                        .retrieve()
                        .body(new ParameterizedTypeReference<>() {});

        return responseDTO.getData();
    }

    public void deleteById(UUID id){
        restClient.delete()
                .uri(evacroutesServiceUrl + "/api/evacuation-route/" + id)
                .retrieve()
                .toBodilessEntity();
    }
}
