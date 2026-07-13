package com.ValleSol.notificationservice;

import com.ValleSol.notificationservice.dto.AlertaEventDTO;
import com.ValleSol.notificationservice.dto.UserDTO;
import com.ValleSol.notificationservice.service.NotificacionStrategy;
import com.ValleSol.notificationservice.service.NotificationService;
import com.ValleSol.notificationservice.service.UserClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceApplicationTests {

    @Mock
    private UserClient userClient;

    @Mock
    private NotificacionStrategy strategy;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void processNotifications_deberiaUsarEmailPorDefectoCuandoNoHayCanales() {
        // Arrange
        UUID despachoId = UUID.randomUUID();

        AlertaEventDTO event = new AlertaEventDTO();
        event.setMensaje("Incendio en Zona Norte");
        event.setNivelEmergencia("ALTO");
        event.setDespachoId(despachoId);
        event.setCanalEmail(false);
        event.setCanalPush(false);

        UserDTO usuario = new UserDTO();
        usuario.setEmail("usuario@test.cl");

        List<UserDTO> usuarios = List.of(usuario);

        when(userClient.getUsersForAlerts()).thenReturn(usuarios);

        // Act
        notificationService.processNotifications(event);

        // Assert
        assertTrue(event.isCanalEmail());
        assertFalse(event.isCanalPush());

        verify(strategy).ejecutar(
                eq(event),
                eq(usuarios),
                eq(despachoId),
                eq("EMAIL")
        );
    }

    @Test
    void processNotifications_deberiaGenerarDespachoIdCuandoNoViene() {
        // Arrange
        AlertaEventDTO event = new AlertaEventDTO();
        event.setMensaje("Alerta critica");
        event.setNivelEmergencia("CRITICO");
        event.setDespachoId(null);
        event.setCanalEmail(true);
        event.setCanalPush(true);

        List<UserDTO> usuarios = List.of();

        when(userClient.getUsersForAlerts()).thenReturn(usuarios);

        // Act
        notificationService.processNotifications(event);

        // Assert
        ArgumentCaptor<UUID> captor =
                ArgumentCaptor.forClass(UUID.class);

        verify(strategy).ejecutar(
                eq(event),
                eq(usuarios),
                captor.capture(),
                eq("EMAIL+PUSH")
        );

        assertNotNull(captor.getValue());
    }
}
