package com.vallesol.bff.dtos.response;

import com.vallesol.bff.enums.ZoneType;
import lombok.Data;

import java.util.UUID;

@Data
public class ZoneResponseDTO {
    private UUID id;
    private String name;
    private String color;
    private ZoneType zoneType;
    private String geoJson;
}
