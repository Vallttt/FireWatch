package com.vallesol.bff.dtos.request;

import lombok.Data;

import java.util.UUID;

@Data
public class ZoneMapDTO {

    private UUID id;
    private String name;
    private String description;
    private String color;
    private String zoneType;
    private String geojson;
    private Boolean isActive;
}
