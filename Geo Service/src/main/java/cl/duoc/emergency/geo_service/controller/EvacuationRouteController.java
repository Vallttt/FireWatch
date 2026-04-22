package cl.duoc.emergency.geo_service.controller;

import cl.duoc.emergency.geo_service.dto.request.EvacuationRouteRequestDTO;
import cl.duoc.emergency.geo_service.dto.response.ApiResponseDTO;
import cl.duoc.emergency.geo_service.dto.response.EvacuationResponseDTO;
import cl.duoc.emergency.geo_service.service.EvacuationRouteService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/evacroute")
@AllArgsConstructor
public class EvacuationRouteController {
    private final EvacuationRouteService evacuationRouteService;

    @PostMapping
    public ResponseEntity<ApiResponseDTO<EvacuationResponseDTO>> createEvacuationRoute(
            @Valid @RequestBody EvacuationRouteRequestDTO evacuationRouteRequestDTO){

        EvacuationResponseDTO rute =
                evacuationRouteService.createEvacuationRoute(
                        evacuationRouteRequestDTO);

        ApiResponseDTO<EvacuationResponseDTO> responseDTO =
                new ApiResponseDTO<>(
                        true, "Ruta de evacuacion creada con exito", rute);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<EvacuationResponseDTO>>> findAll(){

        List<EvacuationResponseDTO> rutes = evacuationRouteService.findAll();

        ApiResponseDTO<List<EvacuationResponseDTO>> responseDTO = new ApiResponseDTO<>(true,"Lista de rutas generada con exito", rutes);

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<EvacuationResponseDTO>> findById(@PathVariable UUID id){

        EvacuationResponseDTO rute = evacuationRouteService.findById(id);

        ApiResponseDTO<EvacuationResponseDTO> responseDTO = new ApiResponseDTO<>(true, "La ruta se cargo correctamente", rute);

        return ResponseEntity.ok(responseDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<EvacuationResponseDTO>> update(
            @Valid @RequestBody EvacuationRouteRequestDTO evacuationRequest,@PathVariable UUID id){

        EvacuationResponseDTO rute = evacuationRouteService.update(evacuationRequest, id);

        ApiResponseDTO<EvacuationResponseDTO> responseDTO = new ApiResponseDTO<>(true, "Ruta actualizada con exito", rute);

        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteById(@PathVariable UUID id) {

        evacuationRouteService.deleteById(id);

        ApiResponseDTO<Void> responseDTO =
                new ApiResponseDTO<>(
                        true,
                        "Ruta eliminada con éxito",
                        null
                );

        return ResponseEntity.ok(responseDTO);
    }


}
