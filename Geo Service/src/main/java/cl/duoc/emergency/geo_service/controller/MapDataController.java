package cl.duoc.emergency.geo_service.controller;

import cl.duoc.emergency.geo_service.dto.response.ApiResponseDTO;
import cl.duoc.emergency.geo_service.dto.response.MapDataResponseDTO;
import cl.duoc.emergency.geo_service.service.MapDataService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/map-data")
@AllArgsConstructor
public class MapDataController {

    private final MapDataService mapDataService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<MapDataResponseDTO>> getMapData() {

        MapDataResponseDTO data = mapDataService.getMapData();

        ApiResponseDTO<MapDataResponseDTO> response =
                new ApiResponseDTO<>(true, "Datos del mapa obtenidos correctamente", data);

        return ResponseEntity.ok(response);
    }
}