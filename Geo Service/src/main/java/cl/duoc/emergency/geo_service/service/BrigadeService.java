package cl.duoc.emergency.geo_service.service;

import cl.duoc.emergency.geo_service.dto.request.BrigadeRequestDTO;
import cl.duoc.emergency.geo_service.dto.response.BrigadeResponseDTO;
import cl.duoc.emergency.geo_service.enums.ZoneType;
import cl.duoc.emergency.geo_service.model.Brigade;
import cl.duoc.emergency.geo_service.model.Zone;
import cl.duoc.emergency.geo_service.repository.BrigadeRepository;
import cl.duoc.emergency.geo_service.repository.ZonesRepository;
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

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class BrigadeService {
    private final BrigadeRepository brigadeRepository;
    private final ZonesRepository zonesRepository;
    private final ModelMapper modelMapper;

    public BrigadeResponseDTO createBrigade(BrigadeRequestDTO brigadeRequestDTO) {

        Zone zone = zonesRepository.findById(brigadeRequestDTO.getZoneId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "La zona no existe"
                ));

        validateBrigadeInsideOperationalZone(brigadeRequestDTO, zone);

        Brigade brigade = new Brigade();
        brigade.setName(brigadeRequestDTO.getName());
        brigade.setInstitution(brigadeRequestDTO.getInstitution());
        brigade.setStatus(brigadeRequestDTO.getStatus());
        brigade.setLatitude(brigadeRequestDTO.getLatitude());
        brigade.setLongitude(brigadeRequestDTO.getLongitude());
        brigade.setZone(zone);

        Brigade savedBrigade = brigadeRepository.save(brigade);

        return mapToResponse(savedBrigade);
    }

    public List<BrigadeResponseDTO> findAll() {
        return brigadeRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    public BrigadeResponseDTO findById(UUID id) {
        Brigade brigade = brigadeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "La brigada no existe"
                ));

        return mapToResponse(brigade);
    }

    public BrigadeResponseDTO updateBrigade(BrigadeRequestDTO brigadeRequestDTO, UUID id) {
        Brigade brigade = brigadeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "La brigada no existe"
                ));

        Zone zone = zonesRepository.findById(brigadeRequestDTO.getZoneId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "La zona no existe"
                ));

        validateBrigadeInsideOperationalZone(brigadeRequestDTO, zone);

        brigade.setName(brigadeRequestDTO.getName());
        brigade.setInstitution(brigadeRequestDTO.getInstitution());
        brigade.setStatus(brigadeRequestDTO.getStatus());
        brigade.setLatitude(brigadeRequestDTO.getLatitude());
        brigade.setLongitude(brigadeRequestDTO.getLongitude());
        brigade.setZone(zone);

        Brigade updatedBrigade = brigadeRepository.save(brigade);

        return mapToResponse(updatedBrigade);
    }

    public void deleteById(UUID id) {
        if (!brigadeRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "La brigada no existe"
            );
        }

        brigadeRepository.deleteById(id);
    }

    private BrigadeResponseDTO mapToResponse(Brigade brigade) {
        BrigadeResponseDTO response = modelMapper.map(brigade, BrigadeResponseDTO.class);
        if (brigade.getZone() != null) {
            response.setZoneId(brigade.getZone().getId());
            response.setZoneName(brigade.getZone().getName());
        }
        return response;
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

    private void validateBrigadeInsideOperationalZone(BrigadeRequestDTO dto, Zone zone) {

        if (zone.getZoneType() != ZoneType.OPERATIONAL) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La brigada solo puede asignarse a una zona operativa"
            );
        }

        Geometry zoneGeometry = geoJsonToGeometry(zone.getGeoJson());

        GeometryFactory geometryFactory = new GeometryFactory();

        Point brigadePoint = geometryFactory.createPoint(
                new Coordinate(dto.getLongitude(), dto.getLatitude())
        );

        if (!zoneGeometry.contains(brigadePoint)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La brigada debe estar dentro de la zona asignada"
            );
        }
    }
}

