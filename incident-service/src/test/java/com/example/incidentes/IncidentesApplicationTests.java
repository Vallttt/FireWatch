package com.example.incidentes;

import com.example.incidentes.dto.request.IncidenteRequestDTO;
import com.example.incidentes.dto.response.IncidenteResponseDTO;
import com.example.incidentes.enums.ReportStatus;
import com.example.incidentes.model.Incidente;
import com.example.incidentes.repository.IncidenteRepository;
import com.example.incidentes.service.IncidenteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentesApplicationTests {

    @Mock
    private IncidenteRepository incidenteRepository;

    @Mock
    private RestTemplate restTemplate;

    private IncidenteService incidenteService;

    @BeforeEach
    void setUp() {

        incidenteService = new IncidenteService();

        ReflectionTestUtils.setField(
                incidenteService,
                "incidenteRepository",
                incidenteRepository
        );

        ReflectionTestUtils.setField(
                incidenteService,
                "restTemplate",
                restTemplate
        );
    }

    @Test
    void crearIncidente_deberiaGuardarIncidenteActivo() {

        UUID reporteId = UUID.randomUUID();
        UUID incidenteId = UUID.randomUUID();

        IncidenteRequestDTO request = new IncidenteRequestDTO();
        request.setReporteId(reporteId);

        when(incidenteRepository.save(any(Incidente.class)))
                .thenAnswer(invocation -> {
                    Incidente incidente = invocation.getArgument(0);
                    incidente.setId(incidenteId);
                    return incidente;
                });

        IncidenteResponseDTO response =
                incidenteService.crearIncidente(request);

        assertNotNull(response);
        assertEquals(incidenteId, response.getId());
        assertEquals(reporteId, response.getReporteId());
        assertEquals(ReportStatus.ACTIVE, response.getEstado());

        ArgumentCaptor<Incidente> captor =
                ArgumentCaptor.forClass(Incidente.class);

        verify(incidenteRepository).save(captor.capture());

        assertEquals(
                ReportStatus.ACTIVE,
                captor.getValue().getEstado()
        );
    }

    @Test
    void obtenerPorId_deberiaLanzarExcepcionCuandoNoExiste() {

        UUID incidenteId = UUID.randomUUID();

        when(incidenteRepository.findById(incidenteId))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> incidenteService.obtenerPorId(incidenteId)
        );

        assertEquals(
                "Incidente no encontrado",
                exception.getMessage()
        );

        verify(incidenteRepository).findById(incidenteId);
    }

}