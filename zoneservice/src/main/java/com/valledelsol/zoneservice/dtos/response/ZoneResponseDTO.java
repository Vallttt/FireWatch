package com.valledelsol.zoneservice.dtos.response;

import com.valledelsol.zoneservice.enums.ZoneType;
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
