package cl.duoc.emergency.geo_service.dto.response;

import cl.duoc.emergency.geo_service.enums.ZoneType;
import lombok.Data;

import java.util.UUID;

@Data
public class ZoneResponseDTO {
    private UUID id;
    private String name;
    private String color;
    private ZoneType type;
    private String geoJson;
}
