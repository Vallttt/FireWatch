package com.ValleSol.notificationservice.controller;

import com.ValleSol.notificationservice.dto.AlertaEventDTO;
import com.ValleSol.notificationservice.dto.UserDTO;
import com.ValleSol.notificationservice.model.Notificacion;
import com.ValleSol.notificationservice.repository.NotificacionRepository;
import com.ValleSol.notificationservice.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/notificaciones")
@CrossOrigin(origins = "*")
public class NotificacionController {

    private final NotificationService    notificationService;
    private final NotificacionRepository repository;

    public NotificacionController(NotificationService notificationService,
                                NotificacionRepository repository) {
        this.notificationService = notificationService;
        this.repository          = repository;
    }


//    @PostMapping("/enviar")
//    public ResponseEntity<String> procesarNotificaciones(@RequestBody AlertaEventDTO event) {
//        notificationService.processNotifications(event);
//        return ResponseEntity.ok("Notifications dispatched successfully");
//    }

    @PostMapping("/enviar")
    public ResponseEntity<String> procesarNotificaciones(
            @RequestBody AlertaEventDTO event) {

        System.out.println("CONTROLLER DESCRIPCION = " + event.getDescripcionReporte());
        System.out.println("CONTROLLER REPORTANTE  = " + event.getUsuarioReportante());
        System.out.println("CONTROLLER LAT         = " + event.getLatitude());

        notificationService.processNotifications(event);

        return ResponseEntity.ok("Notifications dispatched successfully");
    }
   

    @GetMapping
    public ResponseEntity<List<Notificacion>> historial() {
        return ResponseEntity.ok(repository.findOnePerDispatch());
    }

    @GetMapping("/conteo")
    public ResponseEntity<Integer> conteo() {
        return ResponseEntity.ok(repository.countUniqueDispatches());
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        Notificacion notif = repository.findById(id).orElse(null);
        if (notif == null) return ResponseEntity.notFound().build();

        if (notif.getDespachoId() != null) {
            repository.deleteByDespachoId(notif.getDespachoId());
        } else {
            repository.deleteById(id);
        }
        return ResponseEntity.noContent().build();
    }

 

 
    @GetMapping("/brigadas")
    public ResponseEntity<List<Notificacion>> historialBrigadas() {
        return ResponseEntity.ok(repository.findByTipoDestinatario("BRIGADA"));
    }

  
    @GetMapping("/administradores")
    public ResponseEntity<List<Notificacion>> historialAdministradores() {
        return ResponseEntity.ok(repository.findByTipoDestinatario("ADMINISTRADOR"));
    }

  
    @GetMapping("/nivel/{nivel}")
    public ResponseEntity<List<Notificacion>> historialPorNivel(@PathVariable String nivel) {
        return ResponseEntity.ok(repository.findByNivelEmergencia(nivel.toUpperCase()));
    }

 
    @GetMapping("/test-auth")
    public ResponseEntity<List<UserDTO>> testAuth() {
        return ResponseEntity.ok(notificationService.testAuthConnection());
    }
}
