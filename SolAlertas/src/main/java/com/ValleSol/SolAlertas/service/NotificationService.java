package com.ValleSol.SolAlertas.service;

import com.ValleSol.SolAlertas.dto.AlertaRequestDTO;
import com.ValleSol.SolAlertas.dto.UserAlertRequestDTO;
import com.ValleSol.SolAlertas.model.Notificacion;
import com.ValleSol.SolAlertas.repository.NotificacionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final AuthClient authClient;
    private final AlertaFactory factory;
    private final NotificacionRepository repository;

    public NotificationService(AuthClient authClient, AlertaFactory factory, NotificacionRepository repository) {
        this.authClient = authClient;
        this.factory = factory;
        this.repository = repository;
    }

    public List<UserAlertRequestDTO> testAuthConnection() {
        return authClient.getUsersForAlerts();
    }

    /**
     * Procesa y persiste una alerta para todos los usuarios notificables.
     * Canal único: EMAIL. El tipo (Evacuación, Emergencia, etc.) determina
     * solo la clasificación de la alerta, no el canal.
     */
    public void procesarAlerta(AlertaRequestDTO request) {
        List<UserAlertRequestDTO> usuarios = authClient.getUsersForAlerts();
        GeneradorAlerta generador = factory.obtenerGenerador(request.getTipo());

        // UUID compartido por todas las notificaciones de este envío
        UUID despachoId = UUID.randomUUID();

        for (UserAlertRequestDTO user : usuarios) {
            String email = user.getEmail();

            if (email != null && !email.isBlank()) {
                Notificacion alerta = generador.generarAlerta(request.getMensaje(), email);
                alerta.setDespachoId(despachoId);
                alerta.setReporteId(request.getReporteId());
                alerta.setEstadoEnvio("ENVIADO");
                alerta.setUsuarioId(user.getId() != null ? UUID.fromString(user.getId()) : null);
                alerta.setFechaEnvio(LocalDateTime.now());
                repository.save(alerta);
            }
        }
    }
}
