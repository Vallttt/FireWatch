package com.ValleSol.alertservice;

import com.ValleSol.alertservice.dto.AlertaRequestDTO;
import com.ValleSol.alertservice.dto.AlertaResponseDTO;
import com.ValleSol.alertservice.enums.Destinatarios;
import com.ValleSol.alertservice.enums.NivelEmergencia;
import com.ValleSol.alertservice.model.Alerta;
import com.ValleSol.alertservice.repository.AlertaRepository;
import com.ValleSol.alertservice.service.AlertaService;
import com.ValleSol.alertservice.service.ClasificadorEmergencia;
import com.ValleSol.alertservice.service.NotificacionServiceClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertServiceApplicationTests {

    @Mock
    private AlertaRepository alertaRepository;

    @Mock
    private NotificacionServiceClient notificacionServiceClient;

    @Mock
    private ClasificadorEmergencia clasificador;

    @InjectMocks
    private AlertaService alertaService;

    @Test
    void procesarAlerta_deberiaGuardarAlertaYEnviarNotificaciones() {
        // Arrange
        UUID reporteId = UUID.randomUUID();

        AlertaRequestDTO request = new AlertaRequestDTO();
        request.setTipo("incendio");
        request.setMensaje("Incendio forestal en Zona Norte");
        request.setReporteId(reporteId);
        request.setUsuarioRemitente("admin@valledelsol.cl");

        when(clasificador.clasificarNivel(request))
                .thenReturn(NivelEmergencia.ALTO);

        when(clasificador.clasificarDestinatarios(request, NivelEmergencia.ALTO))
                .thenReturn(Destinatarios.BRIGADAS_Y_ADMINISTRADORES);

        // Act
        alertaService.procesarAlerta(request);

        // Assert
        ArgumentCaptor<Alerta> captor =
                ArgumentCaptor.forClass(Alerta.class);

        verify(alertaRepository).save(captor.capture());

        Alerta alertaGuardada = captor.getValue();

        assertEquals("INCENDIO", alertaGuardada.getTipo());
        assertEquals("PROCESADO", alertaGuardada.getEstado());
        assertEquals("ALTO", alertaGuardada.getNivelEmergencia());
        assertEquals(
                "BRIGADAS_Y_ADMINISTRADORES",
                alertaGuardada.getDestinatarios()
        );
        assertEquals(reporteId, alertaGuardada.getReporteId());
        assertNotNull(alertaGuardada.getDespachoId());
        assertNotNull(alertaGuardada.getFechaCreacion());

        // El request queda enriquecido antes de notificar
        assertEquals("ALTO", request.getNivelEmergencia());

        verify(notificacionServiceClient)
                .enviarNotificaciones(eq(request), any(UUID.class));
    }

    @Test
    void listarPorNivel_deberiaFiltrarPorNivelEmergencia() {
        // Arrange
        Alerta critica = new Alerta();
        critica.setId(UUID.randomUUID());
        critica.setNivelEmergencia("CRITICO");
        critica.setMensaje("Alerta critica");

        Alerta baja = new Alerta();
        baja.setId(UUID.randomUUID());
        baja.setNivelEmergencia("BAJO");
        baja.setMensaje("Alerta baja");

        when(alertaRepository.findAll())
                .thenReturn(List.of(critica, baja));

        // Act
        List<AlertaResponseDTO> result =
                alertaService.listarPorNivel("critico");

        // Assert
        assertEquals(1, result.size());
        assertEquals("CRITICO", result.get(0).getNivelEmergencia());
        assertEquals("Alerta critica", result.get(0).getMensaje());

        verify(alertaRepository).findAll();
    }
}
