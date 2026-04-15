package com.ValleSol.SolAlertas.controller;

import java.util.List;

import com.ValleSol.SolAlertas.dto.UserAlertRequestDTO;
import com.ValleSol.SolAlertas.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ValleSol.SolAlertas.dto.AlertaRequestDTO;
import com.ValleSol.SolAlertas.model.Notificacion;
import com.ValleSol.SolAlertas.repository.NotificacionRepository;
import com.ValleSol.SolAlertas.service.AlertaFactory;
import com.ValleSol.SolAlertas.service.GeneradorAlerta;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/alertas")
@CrossOrigin(origins = "*")
public class NotificacionController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificacionRepository repository;

    @Autowired
    private AlertaFactory factory;

    @Autowired
    private NotificationService notificacionService;

    @PostMapping("/enviar")
    public ResponseEntity<String> enviarNuevaAlerta(@RequestBody AlertaRequestDTO request) {
        notificationService.procesarAlerta(request);
        return ResponseEntity.ok("Alertas procesadas correctamente");
    }

    @GetMapping
    public List<Notificacion> historialDeAlertas() {
        return repository.findAll();
    }

    @GetMapping("/test-auth")
    public ResponseEntity<List<UserAlertRequestDTO>> testAuth() {
        return ResponseEntity.ok(notificacionService.testAuthConnection());
    }

    /**
     * Retorna la cantidad total de alertas registradas.
     * Usado por el BFF para el dashboard.
     */
    @GetMapping("/conteo")
    public int conteoDeAlertas() {
        return (int) repository.count();
    }

}