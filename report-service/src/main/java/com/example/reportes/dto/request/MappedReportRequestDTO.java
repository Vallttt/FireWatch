package com.example.reportes.dto.request;

import com.example.reportes.enums.ReportStatus;
import com.example.reportes.enums.SeverityLevel;

import java.time.LocalDateTime;
import java.util.UUID;

public class MappedReportRequestDTO {
    private UUID externalReportId;
    private ReportStatus reportStatus;
    private SeverityLevel severity;
    private Double latitude;
    private Double longitude;
    private LocalDateTime reportedAt;
    private UUID zoneId;

    public UUID getExternalReportId() {
        return externalReportId;
    }

    public void setExternalReportId(UUID externalReportId) {
        this.externalReportId = externalReportId;
    }

    public ReportStatus getReportStatus() {
        return reportStatus;
    }

    public void setReportStatus(ReportStatus reportStatus) {
        this.reportStatus = reportStatus;
    }

    public SeverityLevel getSeverity() {
        return severity;
    }

    public void setSeverity(SeverityLevel severity) {
        this.severity = severity;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }


    public UUID getZoneId() {
        return zoneId;
    }

    public void setZoneId(UUID zoneId) {
        this.zoneId = zoneId;
    }

    public LocalDateTime getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(LocalDateTime reportedAt) {
        this.reportedAt = reportedAt;
    }
}
