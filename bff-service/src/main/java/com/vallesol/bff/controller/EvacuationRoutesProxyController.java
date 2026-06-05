package com.vallesol.bff.controller;

import com.vallesol.bff.client.EvacuationRoutesClient;
import com.vallesol.bff.dtos.request.EvacuationRouteRequestDTO;
import com.vallesol.bff.dtos.response.ApiResponseDTO;
import com.vallesol.bff.dtos.response.EvacuationResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/evacuation-routes")
@RequiredArgsConstructor
public class EvacuationRoutesProxyController {

    private final EvacuationRoutesClient evacuationRoutesClient;

    @GetMapping
    public ResponseEntity<List<EvacuationResponseDTO>> getAllRoutes(){
        return ResponseEntity.ok(evacuationRoutesClient.getAllRoutes());
    }

    @PostMapping
    public ResponseEntity<EvacuationResponseDTO> createRoute(@RequestBody EvacuationRouteRequestDTO requestDTO){
        return ResponseEntity.ok(evacuationRoutesClient.createRoute(requestDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EvacuationResponseDTO> getRouteById(@PathVariable UUID id){
        return ResponseEntity.ok(evacuationRoutesClient.getRouteById(id));
    }

    @GetMapping("/zone/{id}")
    public ResponseEntity<List<EvacuationResponseDTO>> getRoutesByZone(@PathVariable UUID id){
        return ResponseEntity.ok(evacuationRoutesClient.getRoutesByZone(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EvacuationResponseDTO> updateRoute(@PathVariable UUID id, @RequestBody EvacuationRouteRequestDTO requestDTO){
        return ResponseEntity.ok(evacuationRoutesClient.updateRoute(id,requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoute(@PathVariable UUID id){
        evacuationRoutesClient.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
