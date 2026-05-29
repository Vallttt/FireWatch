package cl.duoc.emergency.geo_service.controller;

import cl.duoc.emergency.geo_service.dto.request.MappedReportRequestDTO;
import cl.duoc.emergency.geo_service.dto.response.ApiResponseDTO;
import cl.duoc.emergency.geo_service.dto.response.MappedReportResponseDTO;
import cl.duoc.emergency.geo_service.service.MappedReportService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/geo")
@AllArgsConstructor
public class MappedReportController {

    private final MappedReportService mappedReportService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<MappedReportResponseDTO>>> findAll() {

        List<MappedReportResponseDTO> mappedReports = mappedReportService.findAll();

        ApiResponseDTO<List<MappedReportResponseDTO>> responseDTO =
                new ApiResponseDTO<>(true, "Lista de reportes mapeados traída correctamente", mappedReports);

        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<MappedReportResponseDTO>> createMappedReport(
            @Valid @RequestBody MappedReportRequestDTO mappedReportRequestDTO) {

        MappedReportResponseDTO mappedReport = mappedReportService.createMappedReport(mappedReportRequestDTO);

        ApiResponseDTO<MappedReportResponseDTO> responseDTO =
                new ApiResponseDTO<>(true, "Reporte mapeado creado correctamente", mappedReport);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<MappedReportResponseDTO>> findById(@PathVariable UUID id) {

        MappedReportResponseDTO mappedReport = mappedReportService.findById(id);

        ApiResponseDTO<MappedReportResponseDTO> responseDTO =
                new ApiResponseDTO<>(true, "Reporte mapeado encontrado con éxito", mappedReport);

        return ResponseEntity.ok(responseDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<MappedReportResponseDTO>> updateMappedReport(
            @Valid @RequestBody MappedReportRequestDTO mappedReportRequestDTO,
            @PathVariable UUID id) {

        MappedReportResponseDTO mappedReport =
                mappedReportService.updateMappedReport(mappedReportRequestDTO, id);

        ApiResponseDTO<MappedReportResponseDTO> responseDTO =
                new ApiResponseDTO<>(true, "Reporte mapeado actualizado correctamente", mappedReport);

        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteById(@PathVariable UUID id) {

        mappedReportService.deleteById(id);

        ApiResponseDTO<Void> responseDTO =
                new ApiResponseDTO<>(true, "Reporte mapeado eliminado correctamente", null);

        return ResponseEntity.ok(responseDTO);
    }
}