package cl.duoc.emergency.geo_service.controller;

import cl.duoc.emergency.geo_service.dto.request.BrigadeRequestDTO;
import cl.duoc.emergency.geo_service.dto.response.ApiResponseDTO;
import cl.duoc.emergency.geo_service.dto.response.BrigadeResponseDTO;
import cl.duoc.emergency.geo_service.service.BrigadeService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/brigades")
@AllArgsConstructor
public class BrigadeController {

    private final BrigadeService brigadeService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<BrigadeResponseDTO>>> findAll() {

        List<BrigadeResponseDTO> brigades = brigadeService.findAll();

        ApiResponseDTO<List<BrigadeResponseDTO>> responseDTO =
                new ApiResponseDTO<>(true, "Lista de brigadas traída correctamente", brigades);

        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<BrigadeResponseDTO>> createBrigade(
            @Valid @RequestBody BrigadeRequestDTO brigadeRequestDTO) {

        BrigadeResponseDTO brigade = brigadeService.createBrigade(brigadeRequestDTO);

        ApiResponseDTO<BrigadeResponseDTO> responseDTO =
                new ApiResponseDTO<>(true, "Brigada creada correctamente", brigade);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<BrigadeResponseDTO>> findById(@PathVariable UUID id) {

        BrigadeResponseDTO brigade = brigadeService.findById(id);

        ApiResponseDTO<BrigadeResponseDTO> responseDTO =
                new ApiResponseDTO<>(true, "Brigada encontrada con éxito", brigade);

        return ResponseEntity.ok(responseDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<BrigadeResponseDTO>> updateBrigade(
            @Valid @RequestBody BrigadeRequestDTO brigadeRequestDTO,
            @PathVariable UUID id) {

        BrigadeResponseDTO brigade = brigadeService.updateBrigade(brigadeRequestDTO, id);

        ApiResponseDTO<BrigadeResponseDTO> responseDTO =
                new ApiResponseDTO<>(true, "Brigada actualizada correctamente", brigade);

        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteById(@PathVariable UUID id) {

        brigadeService.deleteById(id);

        ApiResponseDTO<Void> responseDTO =
                new ApiResponseDTO<>(true, "Brigada eliminada correctamente", null);

        return ResponseEntity.ok(responseDTO);
    }
}
