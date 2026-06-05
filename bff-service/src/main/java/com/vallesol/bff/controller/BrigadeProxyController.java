package com.vallesol.bff.controller;

import com.vallesol.bff.client.BrigadeClient;
import com.vallesol.bff.dtos.request.BrigadeRequestDTO;
import com.vallesol.bff.dtos.response.BrigadeResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/brigades")
@RequiredArgsConstructor
public class BrigadeProxyController {

    private final BrigadeClient brigadeClient;

    @GetMapping
    public ResponseEntity<List<BrigadeResponseDTO>> getAllBrigades(){
        return ResponseEntity.ok(brigadeClient.getAllBrigades());
    }

    @PostMapping
    public ResponseEntity<BrigadeResponseDTO> createBrigade(@RequestBody BrigadeRequestDTO requestDTO){
        return ResponseEntity.ok(brigadeClient.createBrigade(requestDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BrigadeResponseDTO> getBrigadeById(@PathVariable UUID id){
        return ResponseEntity.ok(brigadeClient.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BrigadeResponseDTO> updateBrigade(@PathVariable UUID id, @RequestBody BrigadeRequestDTO requestDTO){
        return ResponseEntity.ok(brigadeClient.updateBrigade(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrigade(@PathVariable UUID id){
        brigadeClient.deleteBrigade(id);
        return ResponseEntity.noContent().build();

    }
}
