package cl.duoc.emergency.geo_service.dto.response;

import cl.duoc.emergency.geo_service.enums.BrigadeStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class BrigadeResponseDTO {
    private UUID id;
    private String name;
    private String institution;
    private BrigadeStatus status;
    private Double latitude;
    private Double longitude;
    private UUID zoneId;
    private String zoneName;
}
