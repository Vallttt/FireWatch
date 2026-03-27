package com.ValleSol.SolAlertas.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
@CrossOrigin(origins = "*") //para evitar problemas de CORS, permite solicitudes desde cualquier origen
public class NotificacionController {

    @Autowired
    private NotificacionRepository repository;

    @Autowired
    private AlertaFactory factory;


    @PostMapping("/enviar")
    
    public Notificacion enviarNuevaAlerta(@RequestBody AlertaRequestDTO request) {
        
        // 1. Obtenemos el generador adecuado según el tipo de alerta
        GeneradorAlerta generador = factory.obtenerGenerador(request.tipo());
        
        // 2. Armamos la emergencia con los datos del JSON
        Notificacion alertaArmada = generador.generarAlerta(request.mensaje(), request.destinatario());
        
        // 3. Persistimos en MySQL
        return repository.save(alertaArmada);
    }
    @GetMapping
    public List<Notificacion> historialDeAlertas() {
        return repository.findAll();
    }
    
    

    
    

}
