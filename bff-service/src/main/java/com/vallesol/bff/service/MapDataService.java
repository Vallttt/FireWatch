package com.vallesol.bff.service;

import com.vallesol.bff.client.BrigadeClient;
import com.vallesol.bff.client.EvacuationRoutesClient;
import com.vallesol.bff.client.GeoClient;
import com.vallesol.bff.client.ZoneClient;
import com.vallesol.bff.dtos.response.MapDataResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MapDataService {

    private final ZoneClient zoneClient;
    private final BrigadeClient brigadeClient;
    private final GeoClient geoClient;
    private final EvacuationRoutesClient evacuationRoutesClient;

    public MapDataResponseDTO getMapData() {

        return new MapDataResponseDTO(
                zoneClient.getAllZones(),
                evacuationRoutesClient.getAllRoutes(),
                brigadeClient.getAllBrigades(),
                geoClient.findAllMappedReports()

        );
    }
}