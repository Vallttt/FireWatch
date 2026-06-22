package com.vallesol.bff.controller;

import com.vallesol.bff.client.GeoClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/geo")
@RequiredArgsConstructor
public class GeoProxyController {

    private final GeoClient geoClient;

    @GetMapping()
    public ResponseEntity<?> getAllMappedReports() {
        return ResponseEntity.ok(geoClient.findAllMappedReports());
    }

    /** Borrado lógico: al finalizar un incendio, deja de aparecer en el mapa para siempre (no solo localmente). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMappedReport(@PathVariable UUID id) {
        geoClient.deleteMappedReport(id);
        return ResponseEntity.noContent().build();
    }
}
