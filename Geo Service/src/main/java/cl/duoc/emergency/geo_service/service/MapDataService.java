package cl.duoc.emergency.geo_service.service;

import cl.duoc.emergency.geo_service.dto.response.MapDataResponseDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MapDataService {

    private final ZonesService zonesService;
    private final EvacuationRouteService evacuationRouteService;
    private final BrigadeService brigadeService;
    private final MappedReportService mappedReportService;

    public MapDataResponseDTO getMapData() {

        MapDataResponseDTO response = new MapDataResponseDTO();

        response.setZones(zonesService.findAll());
        response.setRoutes(evacuationRouteService.findAll());
        response.setBrigades(brigadeService.findAll());
        response.setReports(mappedReportService.findAll());

        return response;
    }
}