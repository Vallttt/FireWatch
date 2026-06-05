package com.vallesol.bff.dtos.response;

import com.vallesol.bff.dtos.request.MappedReportMapDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MapDataResponseDTO {

    private List<ZoneResponseDTO> zones;
    private List<BrigadeResponseDTO> routes;
    private List<MappedReportMapDTO> reports;
}