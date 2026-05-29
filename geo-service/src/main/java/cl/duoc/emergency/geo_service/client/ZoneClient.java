package cl.duoc.emergency.geo_service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
}
