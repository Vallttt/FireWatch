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

    public void procesarAlerta(AlertaRequestDTO request) {
        List<UserAlertRequestDTO> usuarios = authClient.getUsersForAlerts();
        GeneradorAlerta generador = factory.obtenerGenerador(request.getTipo());

        // UUID compartido por todas las notificaciones de este envío
        UUID despachoId = UUID.randomUUID();

        for (UserAlertRequestDTO user : usuarios) {
            String destinatario = "SMS".equalsIgnoreCase(request.getTipo())
                    ? user.getPhone()
                    : user.getEmail();

            if (destinatario != null && !destinatario.isBlank()) {
                Notificacion alerta = generador.generarAlerta(request.getMensaje(), destinatario);
                alerta.setDespachoId(despachoId);
                alerta.setReporteId(request.getReporteId());
                alerta.setEstadoEnvio("PENDIENTE");
                alerta.setUsuarioId(user.getId() != null ? UUID.fromString(user.getId()) : null);
                alerta.setFechaEnvio(LocalDateTime.now());
                repository.save(alerta);
            }
        }
    }
}
