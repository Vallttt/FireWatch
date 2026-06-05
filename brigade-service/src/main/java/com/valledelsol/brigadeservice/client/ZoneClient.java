package com.valledelsol.brigadeservice.client;

import com.valledelsol.brigadeservice.dtos.request.ZoneResponseDTO;
import com.valledelsol.brigadeservice.dtos.response.ApiResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ZoneClient {

    private final RestClient restClient;

    @Value("${services.zone.url}")
    private String zoneServiceUrl;

    public boolean existsById(UUID zoneId) {
        try {
            restClient.get()
                    .uri(zoneServiceUrl + "/api/zones/" + zoneId)
                    .retrieve()
                    .toBodilessEntity();

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public ZoneResponseDTO findById(UUID id){

        ApiResponseDTO<ZoneResponseDTO> responseDTO =
                restClient.get()
                        .uri(zoneServiceUrl + "/api/zones/" + id)
                        .retrieve()
                        .body(new ParameterizedTypeReference<>() {});

        return responseDTO.getData();
    }
}
