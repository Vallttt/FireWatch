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
 * Los canales se determinan por los flags canalEmail y canalPush en la solicitud.
 * Si ninguno está seleccionado, se utiliza email por defecto.
 */
    public void processAlert(AlertaRequestDTO request) {
        List<UserAlertRequestDTO> users = authClient.getUsersForAlerts();
        GeneradorAlerta generator = factory.getGenerator(request.getTipo());

        //  UUID compartido para todas las notificaciones que pertenecen a este despacho
        UUID dispatchId = UUID.randomUUID();

        // determinar canales a usar (EMAIL, PUSH, o ambos) según los flags en la solicitud
        boolean useEmail = request.isCanalEmail();
        boolean usePush  = request.isCanalPush();
        if (!useEmail && !usePush) useEmail = true; // valor por defecto si no se especifica ningún canal

        String canal;
        if (useEmail && usePush) {
            canal = "EMAIL+PUSH";
        } else if (usePush) {
            canal = "PUSH";
        } else {
            canal = "EMAIL";
        }

        for (UserAlertRequestDTO user : users) {
            String email = user.getEmail();

            if (email != null && !email.isBlank()) {
                Notificacion alert = generator.generarAlerta(request.getMensaje(), email);
                alert.setDespachoId(dispatchId);
                alert.setReporteId(request.getReporteId());
                alert.setEstadoEnvio("ENVIADO");
                alert.setCanal(canal);
                alert.setUsuarioId(user.getId() != null ? UUID.fromString(user.getId()) : null);
                alert.setFechaEnvio(LocalDateTime.now());
                repository.save(alert);
            }
        }
    }
}
