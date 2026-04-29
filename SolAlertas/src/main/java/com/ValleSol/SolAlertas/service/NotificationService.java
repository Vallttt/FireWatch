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
     * Processes and persists an alert for all notifiable users.
     * Channels are determined by canalEmail and canalPush flags in the request.
     * If neither is selected, email is used as default.
     */
    public void processAlert(AlertaRequestDTO request) {
        List<UserAlertRequestDTO> users = authClient.getUsersForAlerts();
        GeneradorAlerta generator = factory.getGenerator(request.getTipo());

        // Shared UUID for all notifications belonging to this dispatch
        UUID dispatchId = UUID.randomUUID();

        // Determine canal label
        boolean useEmail = request.isCanalEmail();
        boolean usePush  = request.isCanalPush();
        if (!useEmail && !usePush) useEmail = true; // fallback

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
