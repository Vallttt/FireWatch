package com.valledelsol.brigadeservice.dtos.response;


import com.valledelsol.brigadeservice.enums.BrigadeStatus;
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
