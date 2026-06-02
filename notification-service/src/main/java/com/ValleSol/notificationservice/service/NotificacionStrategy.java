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


@Service
public class NotificacionStrategy {

    private final GeneradorEmail   generadorEmail;
    private final GeneradorPush    generadorPush;
    private final GeneradorBrigada generadorBrigada;
    private final GeneradorAdmin   generadorAdmin;
    private final NotificacionRepository repository;

    public NotificacionStrategy(GeneradorEmail generadorEmail,
                                GeneradorPush generadorPush,
                                GeneradorBrigada generadorBrigada,
                                GeneradorAdmin generadorAdmin,
                                NotificacionRepository repository) {
        this.generadorEmail   = generadorEmail;
        this.generadorPush    = generadorPush;
        this.generadorBrigada = generadorBrigada;
        this.generadorAdmin   = generadorAdmin;
        this.repository       = repository;
    }


    public void ejecutar(AlertaEventDTO event,
                        List<UserDTO> todosUsuarios,
                        UUID dispatchId,
                        String canal) {

        List<UserDTO> targets = filtrarAudiencia(todosUsuarios, event.getDestinatarios());

        for (UserDTO user : targets) {
            if (user.getEmail() == null || user.getEmail().isBlank()) continue;

            List<GeneradorAlerta> generators = seleccionarGeneradores(user, event);
            TipoDestinatario tipoDestinatario = resolverTipoDestinatario(user.getRole());

            for (GeneradorAlerta gen : generators) {
                Notificacion notif = gen.generarAlerta(event.getMensaje(), user.getEmail());
                notif.setDespachoId(dispatchId);
                notif.setReporteId(event.getReporteId());
                notif.setEstadoEnvio("ENVIADO");
                notif.setCanal(canal);
                notif.setNivelEmergencia(event.getNivelEmergencia());
                notif.setTipoDestinatario(tipoDestinatario.name());
                notif.setUsuarioId(user.getId() != null ? UUID.fromString(user.getId()) : null);
                notif.setFechaEnvio(LocalDateTime.now());
                repository.save(notif);
            }
        }
    }



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
            default -> todos; // "TODOS" 
        };
    }



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
