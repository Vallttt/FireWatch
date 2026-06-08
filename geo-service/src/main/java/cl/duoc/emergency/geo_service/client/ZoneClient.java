package cl.duoc.emergency.geo_service.client;

import cl.duoc.emergency.geo_service.dto.response.ApiResponseDTO;
import cl.duoc.emergency.geo_service.dto.response.ZoneResponseDTO;
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

    public ZoneResponseDTO existsById(UUID zoneId) {

        ApiResponseDTO<ZoneResponseDTO> responseDTO =
            restClient.get()
                    .uri(zoneServiceUrl + "/api/zones/" + zoneId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

        return responseDTO.getData();

    }
}
