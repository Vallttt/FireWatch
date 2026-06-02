package com.ValleSol.alertservice.service;

import com.ValleSol.alertservice.dto.AlertaRequestDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * HTTP client: alert-service → notification-service.
 *
 * Uses a @LoadBalanced RestTemplate so Spring Cloud + Eureka resolve
 * the logical name "notification-service" to the actual host:port at runtime.
 */
@Service
public class NotificacionServiceClient {

    private static final String NOTIFICATION_BASE = "http://notification-service";

    private final RestTemplate restTemplate;

    public NotificacionServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Forwards the enriched alert event to notification-service.
     * Includes all classification fields so notification-service can
     * apply the correct generator and audience filter.
     *
     * @param request    original (classifier-enriched) alert request
     * @param despachoId shared UUID grouping all resulting notifications
     */
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
            // Non-fatal: Alerta is already persisted; notification-service may be temporarily down
            System.err.println("[alert-service] Warning — notification-service unreachable: " + e.getMessage());
        }
    }
}
