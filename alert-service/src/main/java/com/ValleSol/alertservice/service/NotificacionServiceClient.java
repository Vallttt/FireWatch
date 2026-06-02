package com.ValleSol.alertservice.service;

import com.ValleSol.alertservice.dto.AlertaRequestDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Service
public class NotificacionServiceClient {

    private static final String NOTIFICATION_BASE = "http://notification-service";

    private final RestTemplate restTemplate;

    public NotificacionServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    public void enviarNotificaciones(AlertaRequestDTO request, UUID despachoId) {
        String url = NOTIFICATION_BASE + "/api/notificaciones/enviar";

        Map<String, Object> payload = new HashMap<>();
        payload.put("reporteId",      request.getReporteId() != null ? request.getReporteId().toString() : null);
        payload.put("mensaje",        request.getMensaje());
        payload.put("tipo",           request.getTipo());
        payload.put("nivelEmergencia", request.getNivelEmergencia());
        payload.put("destinatarios",  request.getDestinatarios());
        payload.put("origenAlerta",   request.getOrigenAlerta());
        payload.put("canalEmail",     request.isCanalEmail());
        payload.put("canalPush",      request.isCanalPush());
        payload.put("despachoId",     despachoId.toString());

        try {
            restTemplate.postForEntity(url, payload, Void.class);
        } catch (Exception e) {
            System.err.println("[alert-service] Warning — notification-service unreachable: " + e.getMessage());
        }
    }
}
