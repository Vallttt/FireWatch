package com.valledelsol.brigadeservice.service;


import com.valledelsol.brigadeservice.client.ZoneClient;
import com.valledelsol.brigadeservice.dtos.request.BrigadeRequestDTO;
import com.valledelsol.brigadeservice.dtos.request.ZoneResponseDTO;
import com.valledelsol.brigadeservice.dtos.response.BrigadeResponseDTO;
import com.valledelsol.brigadeservice.model.Brigade;
import com.valledelsol.brigadeservice.repository.BrigadeRepository;
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
    private final ZoneClient zoneClient;
    private final ModelMapper modelMapper;

    public BrigadeResponseDTO createBrigade(BrigadeRequestDTO brigadeRequestDTO) {

        validateBrigadeInsideZone(
                brigadeRequestDTO.getZoneId(),
                brigadeRequestDTO.getLatitude(),
                brigadeRequestDTO.getLongitude()
        );


        Brigade brigade = new Brigade();
        brigade.setName(brigadeRequestDTO.getName());
        brigade.setInstitution(brigadeRequestDTO.getInstitution());
        brigade.setStatus(brigadeRequestDTO.getStatus());
        brigade.setLatitude(brigadeRequestDTO.getLatitude());
        brigade.setLongitude(brigadeRequestDTO.getLongitude());
        brigade.setZoneId(brigadeRequestDTO.getZoneId());

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

        validateBrigadeInsideZone(
                brigadeRequestDTO.getZoneId(),
                brigadeRequestDTO.getLatitude(),
                brigadeRequestDTO.getLongitude()
        );

        brigade.setName(brigadeRequestDTO.getName());
        brigade.setInstitution(brigadeRequestDTO.getInstitution());
        brigade.setStatus(brigadeRequestDTO.getStatus());
        brigade.setLatitude(brigadeRequestDTO.getLatitude());
        brigade.setLongitude(brigadeRequestDTO.getLongitude());
        brigade.setZoneId(brigadeRequestDTO.getZoneId());

        Brigade updatedBrigade = brigadeRepository.save(brigade);

        return mapToResponse(updatedBrigade);
    }

    public void deleteById(UUID id) {
        Brigade brigade = brigadeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "La brigada no existe"
                ));

        brigade.setIsActive(false);
        brigadeRepository.save(brigade);
    }

    private BrigadeResponseDTO mapToResponse(Brigade brigade) {

        BrigadeResponseDTO response = modelMapper.map(brigade, BrigadeResponseDTO.class);

        if (brigade.getZoneId() != null) {
            try {
                ZoneResponseDTO zone = zoneClient.findById(brigade.getZoneId());
                response.setZoneId(zone.getId());
                response.setZoneName(zone.getName());
            } catch (Exception e) {
                response.setZoneName("Zona no disponible");
            }
        }

        return response;
    }




    private void validateBrigadeInsideZone(UUID zoneId, Double latitude, Double longitude) {
        // La zona es opcional: una brigada puede no tener zona asignada.
        if (zoneId == null) {
            return;
        }

        ZoneResponseDTO zone = zoneClient.findById(zoneId);

        Geometry zoneGeometry = geoJsonToGeometry(zone.getGeoJson());

        GeometryFactory geometryFactory = new GeometryFactory();

        Point brigadePoint = geometryFactory.createPoint(
                new Coordinate(longitude, latitude)
        );

        // covers() en vez de contains(): contains() rechaza un punto que cae
        // justo sobre el borde de la zona. buffer(0) normaliza la geometría y
        // una tolerancia pequeña (~50 m) absorbe la imprecisión del marcador
        // colocado sobre el mapa, para no rechazar brigadas que están al borde.
        double toleranciaGrados = 0.0005; // ~50 m
        if (!zoneGeometry.buffer(0).buffer(toleranciaGrados).covers(brigadePoint)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La brigada debe estar dentro de la zona asignada"
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

