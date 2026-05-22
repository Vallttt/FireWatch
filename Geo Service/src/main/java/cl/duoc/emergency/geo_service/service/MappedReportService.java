package cl.duoc.emergency.geo_service.service;

import cl.duoc.emergency.geo_service.dto.request.MappedReportRequestDTO;
import cl.duoc.emergency.geo_service.dto.response.MappedReportResponseDTO;
import cl.duoc.emergency.geo_service.model.MappedReport;
import cl.duoc.emergency.geo_service.model.Zone;
import cl.duoc.emergency.geo_service.repository.MappedReportRepository;
import cl.duoc.emergency.geo_service.repository.ZonesRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class MappedReportService {

    private final MappedReportRepository mappedReportRepository;
    private final ZonesRepository zonesRepository;
    private final ModelMapper modelMapper;

    public MappedReportResponseDTO createMappedReport(MappedReportRequestDTO mappedReportRequestDTO) {
        // Zona es opcional — un reporte puede no tener zona asignada
        Zone zone = null;
        if (mappedReportRequestDTO.getZoneId() != null) {
            zone = zonesRepository.findById(mappedReportRequestDTO.getZoneId())
                    .orElse(null);
        }

        MappedReport mappedReport = new MappedReport();
        mappedReport.setExternalReportId(mappedReportRequestDTO.getExternalReportId());
        mappedReport.setReportStatus(mappedReportRequestDTO.getReportStatus());
        mappedReport.setSeverity(mappedReportRequestDTO.getSeverity());
        mappedReport.setLatitude(mappedReportRequestDTO.getLatitude());
        mappedReport.setLongitude(mappedReportRequestDTO.getLongitude());
        mappedReport.setReportedAt(mappedReportRequestDTO.getReportedAt());
        mappedReport.setLastSyncAt(LocalDateTime.now());
        mappedReport.setZone(zone);

        MappedReport savedMappedReport = mappedReportRepository.save(mappedReport);

        return mapToResponse(savedMappedReport);
    }

    public List<MappedReportResponseDTO> findAll() {
        return mappedReportRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public MappedReportResponseDTO findById(UUID id) {
        MappedReport mappedReport = mappedReportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El reporte mapeado no existe"
                ));

        return mapToResponse(mappedReport);
    }

    public MappedReportResponseDTO updateMappedReport(MappedReportRequestDTO mappedReportRequestDTO, UUID id) {
        MappedReport mappedReport = mappedReportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El reporte mapeado no existe"
                ));

        Zone zone = null;
        if (mappedReportRequestDTO.getZoneId() != null) {
            zone = zonesRepository.findById(mappedReportRequestDTO.getZoneId())
                    .orElse(null);
        }

        mappedReport.setExternalReportId(mappedReportRequestDTO.getExternalReportId());
        mappedReport.setReportStatus(mappedReportRequestDTO.getReportStatus());
        mappedReport.setSeverity(mappedReportRequestDTO.getSeverity());
        mappedReport.setLatitude(mappedReportRequestDTO.getLatitude());
        mappedReport.setLongitude(mappedReportRequestDTO.getLongitude());
        mappedReport.setReportedAt(mappedReportRequestDTO.getReportedAt());
        mappedReport.setLastSyncAt(LocalDateTime.now());
        mappedReport.setZone(zone);

        MappedReport updatedMappedReport = mappedReportRepository.save(mappedReport);

        return mapToResponse(updatedMappedReport);
    }

    public void deleteById(UUID id) {
        if (!mappedReportRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "El reporte mapeado no existe"
            );
        }

        mappedReportRepository.deleteById(id);
    }

    private MappedReportResponseDTO mapToResponse(MappedReport mappedReport) {
        MappedReportResponseDTO response = modelMapper.map(mappedReport, MappedReportResponseDTO.class);
        if (mappedReport.getZone() != null) {
            response.setZoneId(mappedReport.getZone().getId());
        }
        return response;
    }
}