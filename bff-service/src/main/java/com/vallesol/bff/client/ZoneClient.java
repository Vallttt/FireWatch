package com.vallesol.bff.client;


import com.vallesol.bff.dtos.request.ZoneRequestDTO;
import com.vallesol.bff.dtos.response.ApiResponseDTO;
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
public class ZoneClient {

    @Value("${services.zone.url}")
    private String zoneServiceUrl;

    private final RestClient restClient;

    public List<ZoneResponseDTO> getAllZones() {

        ApiResponseDTO<List<ZoneResponseDTO>> response =
                restClient.get()
                        .uri(zoneServiceUrl + "/api/zones")
                        .retrieve()
                        .body(new ParameterizedTypeReference<>() {});

        return response.getData();
    }

    public ZoneResponseDTO getZoneById(UUID id){

        ApiResponseDTO<ZoneResponseDTO> responseDTO =
                restClient.get()
                        .uri(zoneServiceUrl+ "/api/zones/" + id)
                        .retrieve()
                        .body(new ParameterizedTypeReference<>() {});

        return responseDTO.getData();
    }

    public List<ZoneResponseDTO> getAllZonesOperational(){

        ApiResponseDTO<List<ZoneResponseDTO>> responseDTO =
            restClient.get()
                    .uri(zoneServiceUrl + "/api/zones/operational")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

        return responseDTO.getData();
    }

    public List<ZoneResponseDTO> getAllZonesActives(){

        ApiResponseDTO<List<ZoneResponseDTO>> responseDTO =
                restClient.get()
                        .uri(zoneServiceUrl + "/api/zones/active")
                        .retrieve()
                        .body(new ParameterizedTypeReference<>() {});

        return responseDTO.getData();
    }

    public ZoneResponseDTO getMainZone(){

        ApiResponseDTO<ZoneResponseDTO> responseDTO =
                restClient.get()
                        .uri(zoneServiceUrl + "/api/zones/main")
                        .retrieve()
                        .body(new ParameterizedTypeReference<>() {});

        return responseDTO.getData();
    }

    public ZoneResponseDTO createZone(ZoneRequestDTO request){

        ApiResponseDTO<ZoneResponseDTO> responseDTO =
                restClient.post()
                        .uri(zoneServiceUrl + "/api/zones")
                        .body(request)
                        .retrieve()
                        .body(new ParameterizedTypeReference<>() {
                        });

        return responseDTO.getData();
    }

    public ZoneResponseDTO updateZone(UUID id, ZoneRequestDTO request){

        ApiResponseDTO<ZoneResponseDTO> responseDTO =
                restClient.put()
                        .uri(zoneServiceUrl + "/api/zones/" + id)
                        .body(request)
                        .retrieve()
                        .body(new ParameterizedTypeReference<>() {
                        });

        return responseDTO.getData();
    }

    public void deleteZone(UUID id){

        restClient.delete()
                .uri(zoneServiceUrl + "/api/zones/" + id)
                .retrieve()
                .toBodilessEntity();
    }
}
