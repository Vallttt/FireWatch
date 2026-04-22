package cl.duoc.emergency.geo_service.controller;

import cl.duoc.emergency.geo_service.dto.request.ZoneRequestDTO;
import cl.duoc.emergency.geo_service.dto.response.ApiResponseDTO;
import cl.duoc.emergency.geo_service.dto.response.ZoneResponseDTO;
import cl.duoc.emergency.geo_service.service.ZonesService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/zones")
@AllArgsConstructor
public class ZonesController {

    private final ZonesService zonesService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<ZoneResponseDTO>>> findAll() {

        List<ZoneResponseDTO> zones = zonesService.findAll();

        ApiResponseDTO<List<ZoneResponseDTO>> responseDTO =
                new ApiResponseDTO<>(true, "Lista de zonas traída correctamente", zones);

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<ZoneResponseDTO>> findById(@PathVariable UUID id) {

        ZoneResponseDTO zone = zonesService.findById(id);

        ApiResponseDTO<ZoneResponseDTO> responseDTO =
                new ApiResponseDTO<>(true, "Zona encontrada con éxito", zone);

        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<ZoneResponseDTO>> createZone(
            @Valid @RequestBody ZoneRequestDTO zoneRequestDTO) {

        ZoneResponseDTO zone = zonesService.createZone(zoneRequestDTO);

        ApiResponseDTO<ZoneResponseDTO> responseDTO =
                new ApiResponseDTO<>(true, "Zona creada correctamente", zone);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<ZoneResponseDTO>> updateZone(
            @Valid @RequestBody ZoneRequestDTO zoneRequestDTO,
            @PathVariable UUID id) {

        ZoneResponseDTO zone = zonesService.updateZone(zoneRequestDTO, id);

        ApiResponseDTO<ZoneResponseDTO> responseDTO =
                new ApiResponseDTO<>(true, "Zona actualizada correctamente", zone);

        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteById(@PathVariable UUID id) {

        zonesService.deleteById(id);

        ApiResponseDTO<Void> responseDTO =
                new ApiResponseDTO<>(true, "Zona eliminada correctamente", null);

        return ResponseEntity.ok(responseDTO);
    }
}
