package com.vallesol.bff.dtos.response;

import com.vallesol.bff.enums.BrigadeStatus;
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
    private Boolean isActive;
    private UUID zoneId;
    private String zoneName;
}
