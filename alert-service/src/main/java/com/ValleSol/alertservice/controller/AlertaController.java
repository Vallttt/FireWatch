package com.ValleSol.alertservice.controller;

import com.ValleSol.alertservice.dto.AlertaRequestDTO;
import com.ValleSol.alertservice.dto.AlertaResponseDTO;
import com.ValleSol.alertservice.service.AlertaCriticaService;
import com.ValleSol.alertservice.service.AlertaAutomaticaService;
import com.ValleSol.alertservice.service.AlertaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/api/alertas")
@CrossOrigin(origins = "*")
public class AlertaController {

    private final AlertaService           alertaService;
    private final AlertaCriticaService    alertaCriticaService;
    private final AlertaAutomaticaService alertaAutomaticaService;

    public AlertaController(AlertaService alertaService,
                            AlertaCriticaService alertaCriticaService,
                            AlertaAutomaticaService alertaAutomaticaService) {
        this.alertaService           = alertaService;
        this.alertaCriticaService    = alertaCriticaService;
        this.alertaAutomaticaService = alertaAutomaticaService;
    }


    @PostMapping("/enviar")
    public ResponseEntity<String> enviarAlerta(@RequestBody AlertaRequestDTO request) {
        alertaService.procesarAlerta(request);
        return ResponseEntity.ok("Alerts processed successfully");
    }



    @PostMapping("/critica")
    public ResponseEntity<String> emitirAlertaCritica(@RequestBody Map<String, String> body) {
        String descripcion = body.getOrDefault("descripcion", "Zona crítica detectada");
        String reporteIdStr = body.get("reporteId");
        UUID reporteId = reporteIdStr != null ? UUID.fromString(reporteIdStr) : null;
        alertaCriticaService.emitirAlertaZonaCritica(descripcion, reporteId);
        return ResponseEntity.ok("Critical alert dispatched");
    }

    @PostMapping("/critica/maxima")
    public ResponseEntity<String> emitirEmergenciaMaxima(@RequestBody Map<String, String> body) {
        String descripcion = body.getOrDefault("descripcion", "Emergencia máxima activa");
        String reporteIdStr = body.get("reporteId");
        UUID reporteId = reporteIdStr != null ? UUID.fromString(reporteIdStr) : null;
        alertaCriticaService.emitirEmergenciaMaxima(descripcion, reporteId);
        return ResponseEntity.ok("Maximum emergency alert dispatched");
    }


    @PostMapping("/automatica")
    public ResponseEntity<String> alertaAutomatica(@RequestBody Map<String, String> body) {
        String reporteIdStr = body.get("reporteId");
        UUID   reporteId    = reporteIdStr != null ? UUID.fromString(reporteIdStr) : null;
        String descripcion  = body.getOrDefault("descripcion", "Nuevo incidente reportado");
        String severidad    = body.getOrDefault("severidad", "MEDIUM");
        alertaAutomaticaService.generarAlertaDesdeReporte(reporteId, descripcion, severidad);
        return ResponseEntity.ok("Automatic alert generated");
    }


    @PostMapping("/sistema")
    public ResponseEntity<String> alertaSistema(@RequestBody Map<String, String> body) {
        String mensaje = body.getOrDefault("mensaje", "Aviso del sistema Valle del Sol");
        alertaAutomaticaService.generarAlertaDeSistema(mensaje);
        return ResponseEntity.ok("System alert dispatched");
    }


    @GetMapping
    public ResponseEntity<List<AlertaResponseDTO>> listarAlertas() {
        return ResponseEntity.ok(alertaService.listarAlertas());
    }

    @GetMapping("/conteo")
    public ResponseEntity<Long> contarAlertas() {
        return ResponseEntity.ok(alertaService.contarAlertas());
    }

    @GetMapping("/nivel/{nivel}")
    public ResponseEntity<List<AlertaResponseDTO>> listarPorNivel(@PathVariable String nivel) {
        return ResponseEntity.ok(alertaService.listarPorNivel(nivel));
    }
}
