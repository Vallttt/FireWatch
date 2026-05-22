package cl.duoc.emergency.geo_service.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class MapDataResponseDTO {

    private List<ZoneResponseDTO> zones;
    private List<EvacuationResponseDTO> routes;
    private List<BrigadeResponseDTO> brigades;
    private List<MappedReportResponseDTO> reports;
}