package com.ValleSol.SolAlertas.controller;

import java.util.List;

import com.ValleSol.SolAlertas.dto.UserAlertRequestDTO;
import com.ValleSol.SolAlertas.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import com.ValleSol.SolAlertas.dto.AlertaRequestDTO;
import com.ValleSol.SolAlertas.model.Notificacion;
import com.ValleSol.SolAlertas.repository.NotificacionRepository;
import com.ValleSol.SolAlertas.service.AlertaFactory;
import com.ValleSol.SolAlertas.service.GeneradorAlerta;



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
    public ResponseEntity<String> sendNewAlert(@RequestBody AlertaRequestDTO request) {
        notificationService.processAlert(request);
        return ResponseEntity.ok("Alerts processed successfully");
    }

    @GetMapping
    public List<Notificacion> alertHistory() {
        return repository.findOnePerDispatch();
    }

    @GetMapping("/test-auth")
    public ResponseEntity<List<UserAlertRequestDTO>> testAuth() {
        return ResponseEntity.ok(notificacionService.testAuthConnection());
    }

    /**
 * Devuelve el número total de alertas registradas.
 * Utilizado por el BFF para el dashboard.
 */
    @GetMapping("/conteo")
    public int alertCount() {
        return (int) repository.count();
    }

    @DeleteMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<Void> deleteAlert(@PathVariable UUID id) {
        Notificacion notif = repository.findById(id).orElse(null);
        if (notif == null) return ResponseEntity.notFound().build();

        if (notif.getDespachoId() != null) {
            // Eliminar todas las notificaciones que pertenecen al mismo despacho
            repository.deleteByDespachoId(notif.getDespachoId());
        } else {
            repository.deleteById(id);
        }
        return ResponseEntity.noContent().build();
    }

}
