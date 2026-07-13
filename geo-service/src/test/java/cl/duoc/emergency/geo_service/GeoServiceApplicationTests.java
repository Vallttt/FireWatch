package cl.duoc.emergency.geo_service;

import cl.duoc.emergency.geo_service.client.ZoneClient;
import cl.duoc.emergency.geo_service.dto.request.MappedReportRequestDTO;
import cl.duoc.emergency.geo_service.dto.response.MappedReportResponseDTO;
import cl.duoc.emergency.geo_service.dto.response.ZoneResponseDTO;
import cl.duoc.emergency.geo_service.enums.ReportStatus;
import cl.duoc.emergency.geo_service.enums.SeverityLevel;
import cl.duoc.emergency.geo_service.model.MappedReport;
import cl.duoc.emergency.geo_service.repository.MappedReportRepository;
import cl.duoc.emergency.geo_service.service.MappedReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeoServiceApplicationTests {

    // Cuadrado que cubre Santiago aprox: lon -71 a -70, lat -34 a -33
    private static final String GEOJSON_ZONA =
            "{\"type\":\"Polygon\",\"coordinates\":" +
            "[[[-71.0,-34.0],[-70.0,-34.0],[-70.0,-33.0],[-71.0,-33.0],[-71.0,-34.0]]]}";

    @Mock
    private MappedReportRepository mappedReportRepository;

    @Mock
    private ZoneClient zoneClient;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private MappedReportService mappedReportService;

    private UUID zoneId;
    private ZoneResponseDTO zone;

    @BeforeEach
    void setUp() {
        zoneId = UUID.randomUUID();

        zone = new ZoneResponseDTO();
        zone.setId(zoneId);
        zone.setName("Zona Norte");
        zone.setGeoJson(GEOJSON_ZONA);

        when(zoneClient.existsById(zoneId)).thenReturn(zone);
    }

    @Test
    void createMappedReport_deberiaGuardarReporteDentroDeZona() {
        // Arrange
        UUID reportId = UUID.randomUUID();

        MappedReportRequestDTO request = new MappedReportRequestDTO();
        request.setExternalReportId("REP-001");
        request.setReportStatus(ReportStatus.ACTIVE);
        request.setSeverity(SeverityLevel.HIGH);
        request.setLatitude(-33.5);   // dentro del cuadrado
        request.setLongitude(-70.5);
        request.setReportedAt(LocalDateTime.now());
        request.setZoneId(zoneId);

        when(mappedReportRepository.save(any(MappedReport.class)))
                .thenAnswer(invocation -> {
                    MappedReport report = invocation.getArgument(0);
                    report.setId(reportId);
                    return report;
                });

        when(modelMapper.map(any(MappedReport.class), eq(MappedReportResponseDTO.class)))
                .thenAnswer(invocation -> {
                    MappedReport report = invocation.getArgument(0);
                    MappedReportResponseDTO dto = new MappedReportResponseDTO();
                    dto.setId(report.getId());
                    dto.setExternalReportId(report.getExternalReportId());
                    dto.setZoneId(report.getZoneId());
                    return dto;
                });

        // Act
        MappedReportResponseDTO response =
                mappedReportService.createMappedReport(request);

        // Assert
        assertNotNull(response);
        assertEquals(reportId, response.getId());
        assertEquals("REP-001", response.getExternalReportId());
        assertEquals(zoneId, response.getZoneId());

        ArgumentCaptor<MappedReport> captor =
                ArgumentCaptor.forClass(MappedReport.class);

        verify(mappedReportRepository).save(captor.capture());

        MappedReport reporteGuardado = captor.getValue();

        assertEquals(SeverityLevel.HIGH, reporteGuardado.getSeverity());
        assertNotNull(reporteGuardado.getLastSyncAt());

        verify(zoneClient).existsById(zoneId);
    }

    @Test
    void createMappedReport_deberiaRechazarReporteFueraDeZona() {
        // Arrange
        MappedReportRequestDTO request = new MappedReportRequestDTO();
        request.setExternalReportId("REP-002");
        request.setReportStatus(ReportStatus.ACTIVE);
        request.setSeverity(SeverityLevel.LOW);
        request.setLatitude(10.0);   // fuera del cuadrado
        request.setLongitude(10.0);
        request.setReportedAt(LocalDateTime.now());
        request.setZoneId(zoneId);

        // Act
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> mappedReportService.createMappedReport(request)
        );

        // Assert
        assertEquals(400, exception.getStatusCode().value());
        assertEquals(
                "El reporte debe estar dentro de la zona asignada",
                exception.getReason()
        );

        verify(mappedReportRepository, never()).save(any());
    }
}
