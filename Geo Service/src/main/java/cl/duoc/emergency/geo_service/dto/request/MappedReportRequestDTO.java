package cl.duoc.emergency.geo_service.dto.request;

import cl.duoc.emergency.geo_service.enums.ReportStatus;
import cl.duoc.emergency.geo_service.enums.SeverityLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MappedReportRequestDTO {
    @NotBlank
    private String externalReportId;
    @NotNull
    private ReportStatus reportStatus;
    @NotNull
    private SeverityLevel severity;
    @NotNull
    @Range(min = -90, max = 90)
    private Double latitude;
    @NotNull
    @Range(min = -180, max = 180)
    private Double longitude;
    @NotNull
    private LocalDateTime reportedAt;
    private UUID zoneId;
}