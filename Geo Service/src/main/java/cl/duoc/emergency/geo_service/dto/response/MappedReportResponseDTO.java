package cl.duoc.emergency.geo_service.dto.response;

import cl.duoc.emergency.geo_service.enums.ReportStatus;
import cl.duoc.emergency.geo_service.enums.SeverityLevel;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MappedReportResponseDTO {

    private UUID id;
    private String externalReportId;
    private ReportStatus reportStatus;
    private SeverityLevel severity;
    private Double latitude;
    private Double longitude;
    private LocalDateTime reportedAt;
    private LocalDateTime lastSyncAt;
    private UUID zoneId;
}