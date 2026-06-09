package cl.duoc.emergency.geo_service.service;

import cl.duoc.emergency.geo_service.client.ZoneClient;
import cl.duoc.emergency.geo_service.dto.request.MappedReportRequestDTO;
import cl.duoc.emergency.geo_service.dto.response.MappedReportResponseDTO;
import cl.duoc.emergency.geo_service.dto.response.ZoneResponseDTO;
import cl.duoc.emergency.geo_service.model.MappedReport;
import cl.duoc.emergency.geo_service.repository.MappedReportRepository;
import lombok.AllArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.geojson.GeoJsonReader;
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
    private final ZoneClient zoneClient;
    private final ModelMapper modelMapper;

    public MappedReportResponseDTO createMappedReport(MappedReportRequestDTO mappedReportRequestDTO) {

        validateZone(
                mappedReportRequestDTO.getZoneId(),
                mappedReportRequestDTO.getLatitude(),
                mappedReportRequestDTO.getLongitude());

        MappedReport mappedReport = new MappedReport();
        mappedReport.setExternalReportId(mappedReportRequestDTO.getExternalReportId());
        mappedReport.setReportStatus(mappedReportRequestDTO.getReportStatus());
        mappedReport.setSeverity(mappedReportRequestDTO.getSeverity());
        mappedReport.setLatitude(mappedReportRequestDTO.getLatitude());
        mappedReport.setLongitude(mappedReportRequestDTO.getLongitude());
        mappedReport.setReportedAt(mappedReportRequestDTO.getReportedAt());
        mappedReport.setLastSyncAt(LocalDateTime.now());
        mappedReport.setZoneId(mappedReportRequestDTO.getZoneId());

        MappedReport savedMappedReport = mappedReportRepository.save(mappedReport);

        return mapToResponse(savedMappedReport);
    }

    public List<MappedReportResponseDTO> findAll() {
        return mappedReportRepository.findByIsActiveTrue()
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

        if (!Boolean.TRUE.equals(mappedReport.getIsActive())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "El reporte mapeado no existe o está inactivo"
            );
        }

        return mapToResponse(mappedReport);
    }

    public MappedReportResponseDTO updateMappedReport(MappedReportRequestDTO mappedReportRequestDTO, UUID id) {
        MappedReport mappedReport = mappedReportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El reporte mapeado no existe"
                ));

        validateZone(
                mappedReportRequestDTO.getZoneId(),
                mappedReportRequestDTO.getLatitude(),
                mappedReportRequestDTO.getLongitude());

        mappedReport.setExternalReportId(mappedReportRequestDTO.getExternalReportId());
        mappedReport.setReportStatus(mappedReportRequestDTO.getReportStatus());
        mappedReport.setSeverity(mappedReportRequestDTO.getSeverity());
        mappedReport.setLatitude(mappedReportRequestDTO.getLatitude());
        mappedReport.setLongitude(mappedReportRequestDTO.getLongitude());
        mappedReport.setReportedAt(mappedReportRequestDTO.getReportedAt());
        mappedReport.setLastSyncAt(LocalDateTime.now());
        mappedReport.setZoneId(mappedReportRequestDTO.getZoneId());

        MappedReport updatedMappedReport = mappedReportRepository.save(mappedReport);

        return mapToResponse(updatedMappedReport);
    }

    public void deleteById(UUID id) {
        MappedReport map = mappedReportRepository.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Reporte mapeado no existe"
                        ));

        map.setIsActive(false);
        mappedReportRepository.save(map);
    }

    private MappedReportResponseDTO mapToResponse(MappedReport mappedReport) {

        return modelMapper.map(mappedReport, MappedReportResponseDTO.class);
    }

//    private void validateZone(UUID zoneId) {
//
//        if (zoneId != null && !zoneClient.existsById(zoneId)) {
//            throw new ResponseStatusException(
//                    HttpStatus.BAD_REQUEST,
//                    "La zona indicada no existe"
//            );
//        }
//    }

    public void validateZone(UUID id, Double latitude, Double longitude){
        ZoneResponseDTO zone = zoneClient.existsById(id);

        Geometry zoneGeometry = geoJsonToGeometry(zone.getGeoJson());

        GeometryFactory geometryFactory = new GeometryFactory();

        Point reportPoint = geometryFactory.createPoint(
                new Coordinate(longitude, latitude)
        );

        if (!zoneGeometry.contains(reportPoint)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El reporte debe estar dentro de la zona asignada"
            );
        }
    }

    private Geometry geoJsonToGeometry(String geoJson) {
        try {
            GeoJsonReader reader = new GeoJsonReader();
            return reader.read(geoJson);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "GeoJSON inválido"
            );
        }
    }
}