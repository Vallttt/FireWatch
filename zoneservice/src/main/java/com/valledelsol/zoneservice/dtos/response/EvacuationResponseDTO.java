package com.valledelsol.zoneservice.dtos.response;

import lombok.Data;

import java.util.UUID;

@Data
public class EvacuationResponseDTO {

    private UUID id;
    private String name;
    private String description;
    private String geoJson;
    private UUID zoneId;
}