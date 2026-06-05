package com.vallesol.bff.controller;

import com.vallesol.bff.client.ZoneClient;
import com.vallesol.bff.dtos.request.ZoneMapDTO;
import com.vallesol.bff.dtos.request.ZoneRequestDTO;
import com.vallesol.bff.dtos.response.ZoneResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/zones")
@RequiredArgsConstructor
public class ZoneProxyController {

    private final ZoneClient zoneClient;

    @GetMapping
    public ResponseEntity<List<ZoneResponseDTO>> getAllZones() {
        return ResponseEntity.ok(zoneClient.getAllZones());
    }

    @GetMapping("/operational")
    public ResponseEntity<List<ZoneResponseDTO>> getAllZonesOperational() {
        return ResponseEntity.ok(zoneClient.getAllZonesOperational());
    }

    @GetMapping("/active")
    public ResponseEntity<List<ZoneResponseDTO>> getAllZonesActives() {
        return ResponseEntity.ok(zoneClient.getAllZonesActives());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ZoneResponseDTO> getZoneById(@PathVariable UUID id) {
        return ResponseEntity.ok(zoneClient.getZoneById(id));
    }

    @GetMapping("/main")
    public ResponseEntity<ZoneResponseDTO> getMainZone() {
        return ResponseEntity.ok(zoneClient.getMainZone());
    }

    @PostMapping
    public ResponseEntity<ZoneResponseDTO> createZone(
            @RequestBody ZoneRequestDTO request) {
        return ResponseEntity.ok(zoneClient.createZone(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ZoneResponseDTO> updateZone(
            @PathVariable UUID id,
            @RequestBody ZoneRequestDTO request
    ) {
        return ResponseEntity.ok(zoneClient.updateZone(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteZone(@PathVariable UUID id) {
        zoneClient.deleteZone(id);
        return ResponseEntity.noContent().build();
    }
}
