package com.ValleSol.notificationservice.service;

import com.ValleSol.notificationservice.dto.AlertaEventDTO;
import com.ValleSol.notificationservice.dto.UserDTO;
import com.ValleSol.notificationservice.enums.TipoDestinatario;
import com.ValleSol.notificationservice.model.Notificacion;
import com.ValleSol.notificationservice.repository.NotificacionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Core routing engine of the notification-service.
 *
 * For each user in the filtered audience:
 *   1. Selects the correct generator(s) based on user role and active channels.
 *   2. Persists one Notificacion record per (user × generator).
 *   3. Sends a real email for every non-PUSH notification via EmailSenderService.
 *
 * Audience filtering rules:
 *   "TODOS"                    → all users
 *   "BRIGADAS"                 → users with role "BRIGADA"
 *   "ADMINISTRADORES"          → users with role "ADMIN"
 *   "BRIGADAS_Y_ADMINISTRADORES" → brigades + admins only
 *
 * Generator selection per user (role-based):
 *   role == "BRIGADA"  → GeneradorBrigada (always both channels)
 *   role == "ADMIN"    → GeneradorAdmin   (always both channels)
 *   role == "SISTEMA"  → GeneradorEmail   (email only)
 *   other / null       → GeneradorEmail (if canalEmail) + GeneradorPush (if canalPush)
 */
@Service
public class NotificacionStrategy {

    private final GeneradorEmail      generadorEmail;
    private final GeneradorPush       generadorPush;
    private final GeneradorBrigada    generadorBrigada;
    private final GeneradorAdmin      generadorAdmin;
    private final NotificacionRepository repository;
    private final EmailSenderService  emailSenderService;

    public NotificacionStrategy(GeneradorEmail generadorEmail,
                                GeneradorPush generadorPush,
                                GeneradorBrigada generadorBrigada,
                                GeneradorAdmin generadorAdmin,
                                NotificacionRepository repository,
                                EmailSenderService emailSenderService) {
        this.generadorEmail      = generadorEmail;
        this.generadorPush       = generadorPush;
        this.generadorBrigada    = generadorBrigada;
        this.generadorAdmin      = generadorAdmin;
        this.repository          = repository;
        this.emailSenderService  = emailSenderService;
    }

    /**
     * Fan-out notifications: one record per (user × generator).
     * Real emails are sent for EMAIL, BRIGADA and ADMIN types.
     * PUSH type is persisted only (FCM integration pending).
     */
    public void ejecutar(AlertaEventDTO event,
                         List<UserDTO> todosUsuarios,
                         UUID dispatchId,
                         String canal) {

        List<UserDTO> targets = filtrarAudiencia(todosUsuarios, event.getDestinatarios());

        for (UserDTO user : targets) {
            if (user.getEmail() == null || user.getEmail().isBlank()) continue;

            List<GeneradorAlerta> generators    = seleccionarGeneradores(user, event);
            TipoDestinatario     tipoDestinatario = resolverTipoDestinatario(user.getRole());

            for (GeneradorAlerta gen : generators) {
                Notificacion notif =
                        gen.generarAlerta(event, user.getEmail());
                notif.setDespachoId(dispatchId);
                notif.setReporteId(event.getReporteId());
                notif.setEstadoEnvio("ENVIADO");
                notif.setCanal(canal);
                notif.setNivelEmergencia(event.getNivelEmergencia());
                notif.setTipoDestinatario(tipoDestinatario.name());
                notif.setUsuarioId(user.getId() != null ? UUID.fromString(user.getId()) : null);
                notif.setFechaEnvio(LocalDateTime.now());
                notif.setUsuarioRemitente(event.getUsuarioRemitente());
                repository.save(notif);

                // Send real email for all channels except PUSH (FCM pending)
                if (!"PUSH".equals(notif.getTipoAlerta())) {
                    emailSenderService.enviarAlerta(notif);
                }
            }
        }
    }

    // ─── audience filter ─────────────────────────────────────────────────────

    private List<UserDTO> filtrarAudiencia(List<UserDTO> todos, String destinatarios) {
        if (destinatarios == null) return todos;
        return switch (destinatarios.toUpperCase()) {
            case "BRIGADAS" ->
                    todos.stream().filter(u -> "BRIGADA".equalsIgnoreCase(u.getRole()))
                         .collect(Collectors.toList());
            case "ADMINISTRADORES" ->
                    todos.stream().filter(u -> "ADMIN".equalsIgnoreCase(u.getRole()))
                         .collect(Collectors.toList());
            case "BRIGADAS_Y_ADMINISTRADORES" ->
                    todos.stream()
                         .filter(u -> "BRIGADA".equalsIgnoreCase(u.getRole())
                                   || "ADMIN".equalsIgnoreCase(u.getRole()))
                         .collect(Collectors.toList());
            default -> todos;
        };
    }

    // ─── generator selection ─────────────────────────────────────────────────

    private List<GeneradorAlerta> seleccionarGeneradores(UserDTO user, AlertaEventDTO event) {
        String role = user.getRole() != null ? user.getRole().toUpperCase() : "CIUDADANO";
        List<GeneradorAlerta> gens = new ArrayList<>();

        switch (role) {
            case "BRIGADA":
                gens.add(generadorBrigada);
                break;
            case "ADMIN":
                gens.add(generadorAdmin);
                break;
            case "SISTEMA":
                gens.add(generadorEmail);
                break;
            default:
                if (event.isCanalEmail()) gens.add(generadorEmail);
                if (event.isCanalPush())  gens.add(generadorPush);
                if (gens.isEmpty())       gens.add(generadorEmail);
                break;
        }
        return gens;
    }

    // ─── helper ──────────────────────────────────────────────────────────────

    private TipoDestinatario resolverTipoDestinatario(String role) {
        if (role == null) return TipoDestinatario.CIUDADANO;
        return switch (role.toUpperCase()) {
            case "BRIGADA" -> TipoDestinatario.BRIGADA;
            case "ADMIN"   -> TipoDestinatario.ADMINISTRADOR;
            case "SISTEMA" -> TipoDestinatario.SISTEMA;
            default        -> TipoDestinatario.CIUDADANO;
        };
    }
}
