package com.ValleSol.notificationservice.service;

import com.ValleSol.notificationservice.dto.AlertaEventDTO;
import com.ValleSol.notificationservice.dto.UserDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final AuthClient             authClient;
    private final NotificacionStrategy   strategy;

    public NotificationService(AuthClient authClient,
                                NotificacionStrategy strategy) {
        this.authClient = authClient;
        this.strategy   = strategy;
    }

    public void processNotifications(AlertaEventDTO event) {
        List<UserDTO> usuarios = authClient.getUsersForAlerts();

        UUID dispatchId = event.getDespachoId() != null
                ? event.getDespachoId()
                : UUID.randomUUID();

        
        boolean useEmail = event.isCanalEmail();
        boolean usePush  = event.isCanalPush();
        if (!useEmail && !usePush) useEmail = true;
        event.setCanalEmail(useEmail);
        event.setCanalPush(usePush);

        String canal;
        if (useEmail && usePush)  canal = "EMAIL+PUSH";
        else if (usePush)         canal = "PUSH";
        else                      canal = "EMAIL";

        strategy.ejecutar(event, usuarios, dispatchId, canal);
    }

    public List<UserDTO> testAuthConnection() {
        return authClient.getUsersForAlerts();
    }
}
