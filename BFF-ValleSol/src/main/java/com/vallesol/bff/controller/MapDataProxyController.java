package com.vallesol.bff.controller;


import com.vallesol.bff.dtos.response.ApiResponseDTO;
import com.vallesol.bff.dtos.response.MapDataResponseDTO;
import com.vallesol.bff.service.MapDataService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/map-data")
@AllArgsConstructor
public class MapDataProxyController {

    private final MapDataService mapDataService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<MapDataResponseDTO>> getMapData() {

        MapDataResponseDTO data = mapDataService.getMapData();

        ApiResponseDTO<MapDataResponseDTO> response =
                new ApiResponseDTO<>(true, "Datos del mapa obtenidos correctamente", data);

        return ResponseEntity.ok(response);
    }
}