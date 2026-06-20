package com.ValleSol.notificationservice.service;

import com.ValleSol.notificationservice.dto.AlertaEventDTO;
import com.ValleSol.notificationservice.dto.UserDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final UserClient userClient;
    private final NotificacionStrategy   strategy;

    public NotificationService(UserClient userClient,
                               NotificacionStrategy strategy) {
        this.userClient = userClient;
        this.strategy   = strategy;
    }

    public void processNotifications(AlertaEventDTO event) {

        System.out.println("========== EVENTO RECIBIDO ==========");
        System.out.println("Descripcion: " + event.getDescripcionReporte());
        System.out.println("Reportante : " + event.getUsuarioReportante());
        System.out.println("Latitud    : " + event.getLatitude());
        System.out.println("Longitud   : " + event.getLongitude());
        System.out.println("Fecha      : " + event.getFechaReporte());
        System.out.println("=====================================");

        List<UserDTO> usuarios = userClient.getUsersForAlerts();

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
        return userClient.getUsersForAlerts();
    }
}
