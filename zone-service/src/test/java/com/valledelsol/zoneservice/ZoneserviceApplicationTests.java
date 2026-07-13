package com.valledelsol.zoneservice;

import com.valledelsol.zoneservice.dtos.request.ZoneRequestDTO;
import com.valledelsol.zoneservice.enums.ZoneType;
import com.valledelsol.zoneservice.repository.ZonesRepository;
import com.valledelsol.zoneservice.service.ZonesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ZoneserviceApplicationTests {

    @Mock
    private ZonesRepository zonesRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ZonesService zonesService;

    @Test
    void createZone_deberiaRechazarSegundaZonaPrincipalActiva() {
        // Arrange
        ZoneRequestDTO request = new ZoneRequestDTO();
        request.setName("Zona Principal 2");
        request.setDescription("Intento de segunda zona MAIN");
        request.setColor("#FF0000");
        request.setZoneType(ZoneType.MAIN);
        request.setGeoJson("{\"type\":\"Polygon\",\"coordinates\":[[[0,0],[1,0],[1,1],[0,1],[0,0]]]}");
        request.setIsActive(true);

        when(zonesRepository.existsByZoneTypeAndIsActiveTrue(ZoneType.MAIN))
                .thenReturn(true);

        // Act
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> zonesService.createZone(request)
        );

        // Assert
        assertEquals(400, exception.getStatusCode().value());
        assertEquals("Ya existe una zona principal activa", exception.getReason());

        verify(zonesRepository, never()).save(any());
    }

    @Test
    void findById_deberiaLanzarErrorCuandoZonaNoExiste() {
        // Arrange
        UUID zoneId = UUID.randomUUID();

        when(zonesRepository.findById(zoneId))
                .thenReturn(Optional.empty());

        // Act
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> zonesService.findById(zoneId)
        );

        // Assert
        assertEquals(404, exception.getStatusCode().value());
        assertEquals("La zona no existe", exception.getReason());

        verify(zonesRepository).findById(zoneId);
    }
}
