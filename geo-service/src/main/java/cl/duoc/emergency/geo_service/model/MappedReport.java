package cl.duoc.emergency.geo_service.model;

import cl.duoc.emergency.geo_service.enums.ReportStatus;
import cl.duoc.emergency.geo_service.enums.SeverityLevel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "mapped_reports")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MappedReport {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "external_report_id", nullable = false)
    private String externalReportId;
    @Enumerated(EnumType.STRING)
    @Column(name = "report_status",nullable = false)
    private ReportStatus reportStatus;
    @Enumerated(EnumType.STRING)
    @Column(name = "severity",nullable = false)
    private SeverityLevel severity;
    @Column(name = "latitude", nullable = false)
    private Double latitude;
    @Column(name = "longitude", nullable = false)
    private Double longitude;
    @Column(name = "reported_at", nullable = false)
    private LocalDateTime reportedAt;
    @Column(name = "last_sync_at", nullable = false)
    private LocalDateTime lastSyncAt;//ver en logica, ejemplo: mappedReport.setLastSyncAt(LocalDateTime.now());
    @Column(name = "zone_id")
    private UUID zoneId;
}
