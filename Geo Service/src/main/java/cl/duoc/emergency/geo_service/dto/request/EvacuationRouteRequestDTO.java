package cl.duoc.emergency.geo_service.dto.request;

import cl.duoc.emergency.geo_service.model.Zone;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class EvacuationRouteRequestDTO {
    @NotBlank
    @Size(min = 4, max = 50)
    private String name;
    @NotBlank
    private String description;
    @NotBlank
    @Size(min = 20, max = 10000)
    private String geoJson;
    @NotNull
    private UUID zoneId;
}
