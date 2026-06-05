package com.valledelsol.brigadeservice.dtos.request;

import com.valledelsol.brigadeservice.enums.ZoneType;
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
