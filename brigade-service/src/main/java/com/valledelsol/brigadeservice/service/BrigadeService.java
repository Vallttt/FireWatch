package com.valledelsol.brigadeservice.service;


import com.valledelsol.brigadeservice.client.ZoneClient;
import com.valledelsol.brigadeservice.dtos.request.BrigadeRequestDTO;
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

        validateBrigadeZone(brigadeRequestDTO.getZoneId());


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

        validateBrigadeZone(brigadeRequestDTO.getZoneId());

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
        if (!brigadeRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "La brigada no existe"
            );
        }

        brigadeRepository.deleteById(id);
    }

    private BrigadeResponseDTO mapToResponse(Brigade brigade) {

        return modelMapper.map(brigade, BrigadeResponseDTO.class);
    }




    private void validateBrigadeZone(UUID zoneId) {

        if (zoneId != null && !zoneClient.existsById(zoneId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La zona indicada no existe"
            );
        }
    }
}

