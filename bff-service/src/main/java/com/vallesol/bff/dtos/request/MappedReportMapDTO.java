package com.vallesol.bff.dtos.request;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MappedReportMapDTO {

    private UUID id;
    private String externalReportId;
    private String reportStatus;
    private String severity;
    private Double latitude;
    private Double longitude;
    private LocalDateTime reportedAt;
    private LocalDateTime lastSyncAt;
    private UUID zoneId;
}
