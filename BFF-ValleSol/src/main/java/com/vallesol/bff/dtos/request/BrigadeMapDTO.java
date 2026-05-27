package com.vallesol.bff.dtos.request;

import lombok.Data;

import java.util.UUID;

@Data
public class BrigadeMapDTO {

    private UUID id;
    private String name;
    private String institution;
    private String status;
    private Double latitude;
    private Double longitude;
    private UUID zoneId;
}
