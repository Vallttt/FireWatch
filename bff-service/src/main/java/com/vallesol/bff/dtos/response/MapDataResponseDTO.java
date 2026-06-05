package com.vallesol.bff.dtos.response;

import com.vallesol.bff.dtos.request.BrigadeMapDTO;
import com.vallesol.bff.dtos.request.MappedReportMapDTO;
import com.vallesol.bff.dtos.request.ZoneMapDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MapDataResponseDTO {

    private List<ZoneResponseDTO> zones;
    private List<BrigadeMapDTO> routes;
    private List<MappedReportMapDTO> reports;
}