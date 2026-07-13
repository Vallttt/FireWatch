package com.example.reportes;

import com.example.reportes.dto.request.ReporteRequestDTO;
import com.example.reportes.dto.response.ReporteResponseDTO;
import com.example.reportes.model.Reporte;
import com.example.reportes.repository.ReporteRepository;
import com.example.reportes.service.AlertService;
import com.example.reportes.service.EvidenceService;
import com.example.reportes.service.GeoService;
import com.example.reportes.service.IncidentService;
import com.example.reportes.service.ReporteService;
import com.example.reportes.service.ZoneClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportesApplicationTests {

    @Mock
    private ReporteRepository reporteRepository;

    @Mock
    private GeoService geoService;

    @Mock
    private AlertService alertService;

    @Mock
    private IncidentService incidentService;

    @Mock
    private EvidenceService evidenceService;

    @Mock
    private ZoneClient zoneClient;

    private ReporteService reporteService;

    @BeforeEach
    void setUp() {
        reporteService = new ReporteService(
                geoService,
                alertService,
                incidentService,
                evidenceService,
                zoneClient
        );

        ReflectionTestUtils.setField(
                reporteService,
                "reporteRepository",
                reporteRepository
        );
    }

    @Test
    void crearReporte_deberiaGuardarReporteAnonimo() {
        // Arrange
        UUID reporteId = UUID.randomUUID();
        UUID zoneId = UUID.randomUUID();

        ReporteRequestDTO request = new ReporteRequestDTO();
        request.setUsuarioReportante("");
        request.setDescripcion("Incendio forestal cercano a viviendas");
        request.setLatitude(-33.4489);
        request.setLongitude(-70.6693);
        request.setZoneId(zoneId);

        when(reporteRepository.save(any(Reporte.class)))
                .thenAnswer(invocation -> {
                    Reporte reporte = invocation.getArgument(0);
                    reporte.setId(reporteId);
                    return reporte;
                });

        when(zoneClient.obtenerNombreZona(zoneId))
                .thenReturn("Zona Norte");

        // Act
        ReporteResponseDTO response =
                reporteService.crearReporte(request);

        // Assert
        assertNotNull(response);
        assertEquals(reporteId, response.getId());
        assertEquals("Anonimo", response.getUsuarioReportante());
        assertEquals(
                "Incendio forestal cercano a viviendas",
                response.getDescripcion()
        );
        assertEquals(zoneId, response.getZoneId());
        assertNotNull(response.getFechaIncidente());

        ArgumentCaptor<Reporte> captor =
                ArgumentCaptor.forClass(Reporte.class);

        verify(reporteRepository).save(captor.capture());

        Reporte reporteGuardado = captor.getValue();

        assertEquals(
                "Anonimo",
                reporteGuardado.getUsuarioReportante()
        );

        assertEquals(
                zoneId,
                reporteGuardado.getZoneId()
        );

        assertNotNull(
                reporteGuardado.getFechaIncidente()
        );

        verify(incidentService).crearIncidente(any());
        verify(geoService).crearMappedReport(any());
        verify(alertService).enviarAlerta(any());
    }

    @Test
    void obtenerPorId_deberiaLanzarErrorCuandoReporteNoExiste() {
        // Arrange
        UUID reporteId = UUID.randomUUID();

        when(reporteRepository.findById(reporteId))
                .thenReturn(Optional.empty());

        // Act
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> reporteService.obtenerPorId(reporteId)
        );

        // Assert
        assertEquals(
                "Reporte no encontrado",
                exception.getMessage()
        );

        verify(reporteRepository).findById(reporteId);
    }
}