package com.valledelsol.brigadeservice;

import com.valledelsol.brigadeservice.client.ZoneClient;
import com.valledelsol.brigadeservice.dtos.request.BrigadeRequestDTO;
import com.valledelsol.brigadeservice.dtos.response.BrigadeResponseDTO;
import com.valledelsol.brigadeservice.enums.BrigadeStatus;
import com.valledelsol.brigadeservice.model.Brigade;
import com.valledelsol.brigadeservice.repository.BrigadeRepository;
import com.valledelsol.brigadeservice.service.BrigadeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrigadeserviceApplicationTests {

    @Mock
    private BrigadeRepository brigadeRepository;

    @Mock
    private ZoneClient zoneClient;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private BrigadeService brigadeService;

    @Test
    void createBrigade_deberiaGuardarBrigadaSinZonaAsignada() {
        // Arrange
        UUID brigadeId = UUID.randomUUID();

        BrigadeRequestDTO request = new BrigadeRequestDTO();
        request.setName("Brigada Norte");
        request.setInstitution("Bomberos Valle del Sol");
        request.setStatus(BrigadeStatus.AVAILABLE);
        request.setLatitude(-33.4489);
        request.setLongitude(-70.6693);
        request.setZoneId(null);

        when(brigadeRepository.save(any(Brigade.class)))
                .thenAnswer(invocation -> {
                    Brigade brigade = invocation.getArgument(0);
                    brigade.setId(brigadeId);
                    return brigade;
                });

        when(modelMapper.map(any(Brigade.class), eq(BrigadeResponseDTO.class)))
                .thenAnswer(invocation -> {
                    Brigade brigade = invocation.getArgument(0);
                    BrigadeResponseDTO dto = new BrigadeResponseDTO();
                    dto.setId(brigade.getId());
                    dto.setName(brigade.getName());
                    dto.setStatus(brigade.getStatus());
                    return dto;
                });

        // Act
        BrigadeResponseDTO response = brigadeService.createBrigade(request);

        // Assert
        assertNotNull(response);
        assertEquals(brigadeId, response.getId());
        assertEquals("Brigada Norte", response.getName());
        assertEquals(BrigadeStatus.AVAILABLE, response.getStatus());

        ArgumentCaptor<Brigade> captor =
                ArgumentCaptor.forClass(Brigade.class);

        verify(brigadeRepository).save(captor.capture());

        Brigade brigadaGuardada = captor.getValue();

        assertEquals("Bomberos Valle del Sol", brigadaGuardada.getInstitution());
        assertEquals(-33.4489, brigadaGuardada.getLatitude());
        assertEquals(-70.6693, brigadaGuardada.getLongitude());

        // Sin zona asignada no debe consultar al zone-service
        verify(zoneClient, never()).findById(any());
    }

    @Test
    void findById_deberiaLanzarErrorCuandoBrigadaNoExiste() {
        // Arrange
        UUID brigadeId = UUID.randomUUID();

        when(brigadeRepository.findById(brigadeId))
                .thenReturn(Optional.empty());

        // Act
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> brigadeService.findById(brigadeId)
        );

        // Assert
        assertEquals(404, exception.getStatusCode().value());
        assertEquals("La brigada no existe", exception.getReason());

        verify(brigadeRepository).findById(brigadeId);
    }
}
