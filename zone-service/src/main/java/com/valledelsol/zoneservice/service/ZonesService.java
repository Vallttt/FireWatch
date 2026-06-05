package com.valledelsol.zoneservice.service;


import com.valledelsol.zoneservice.dtos.request.ZoneRequestDTO;
import com.valledelsol.zoneservice.dtos.response.ZoneResponseDTO;
import com.valledelsol.zoneservice.enums.ZoneType;
import com.valledelsol.zoneservice.model.Zone;
import com.valledelsol.zoneservice.repository.ZonesRepository;
import lombok.AllArgsConstructor;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ZonesService {
    private final ZonesRepository zonesRepository;
    private final ModelMapper modelMapper;

    public ZoneResponseDTO createZone(ZoneRequestDTO zoneRequestDTO){

        if (zoneRequestDTO.getZoneType() == ZoneType.MAIN &&
        zonesRepository.existsByZoneTypeAndIsActiveTrue(ZoneType.MAIN)) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Ya existe una zona principal activa"
            );
        }
        Zone zone = new Zone();
        zone.setName(zoneRequestDTO.getName());
        zone.setDescription(zoneRequestDTO.getDescription());
        zone.setColor(zoneRequestDTO.getColor());
        zone.setZoneType(zoneRequestDTO.getZoneType());
        zone.setGeoJson(zoneRequestDTO.getGeoJson());
        zone.setIsActive(true);

        if (zoneRequestDTO.getZoneType() == ZoneType.OPERATIONAL) {

            Zone mainZone = zonesRepository
                    .findFirstByZoneTypeAndIsActiveTrue(ZoneType.MAIN)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "No existe zona MAIN"
                    ));

            Geometry mainGeometry = geoJsonToGeometry(mainZone.getGeoJson());
            Geometry newGeometry = geoJsonToGeometry(zoneRequestDTO.getGeoJson());

            if (!mainGeometry.contains(newGeometry)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "La zona debe estar dentro de la zona principal"
                );
            }
        }

        return modelMapper.map(zonesRepository.save(zone), ZoneResponseDTO.class);
    }

    private Geometry geoJsonToGeometry(String geoJson) {
        try {
            GeoJsonReader reader = new GeoJsonReader();
            return reader.read(geoJson);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "GeoJSON inválido");
        }
    }

    public List<ZoneResponseDTO> findAll(){
        return zonesRepository.findByIsActiveTrue()
                .stream()
                .map(zone -> modelMapper.map(zone, ZoneResponseDTO.class))
                .toList();
    }

    public ZoneResponseDTO findById(UUID id){
        Zone zone = zonesRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "La zona no existe"
                ));

        if (!Boolean.TRUE.equals(zone.getIsActive())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "La zona no existe o se encuentra inactiva"
            );
        }

        return modelMapper.map(zone, ZoneResponseDTO.class);

    }

    public ZoneResponseDTO findMainZone() {
    Zone mainZone = zonesRepository.findFirstByZoneTypeAndIsActiveTrue(ZoneType.MAIN)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No existe una zona principal activa"
            ));

    return modelMapper.map(mainZone, ZoneResponseDTO.class);
    }

    public List<ZoneResponseDTO> findOperationalZones() {
        return zonesRepository.findByZoneTypeAndIsActiveTrue(ZoneType.OPERATIONAL)
                .stream()
                .map(zone -> modelMapper.map(zone, ZoneResponseDTO.class))
                .toList();
    }

    public List<ZoneResponseDTO> findActiveZones() {
        return zonesRepository.findByIsActiveTrue()
                .stream()
                .map(zone -> modelMapper.map(zone, ZoneResponseDTO.class))
                .toList();
    }

    public void deleteById(UUID id){
        Zone zone = zonesRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "La zona no existe"
                ));


        //borrado logico
        zone.setIsActive(false);
        zonesRepository.save(zone);

    }

    public ZoneResponseDTO updateZone(ZoneRequestDTO zoneRequestDTO, UUID id) {
    Zone zone = zonesRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "La zona no existe"
            ));

    if (zoneRequestDTO.getZoneType() == ZoneType.OPERATIONAL) {

        Zone mainZone = zonesRepository
                .findFirstByZoneTypeAndIsActiveTrue(ZoneType.MAIN)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "No existe zona MAIN"
                ));

        Geometry mainGeometry = geoJsonToGeometry(mainZone.getGeoJson());
        Geometry newGeometry = geoJsonToGeometry(zoneRequestDTO.getGeoJson());

        if (!mainGeometry.contains(newGeometry)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La zona debe estar dentro de la zona principal"
            );
        }
    }

    if (zoneRequestDTO.getZoneType() == ZoneType.MAIN &&
            zonesRepository.existsByZoneTypeAndIsActiveTrue(ZoneType.MAIN) &&
            zone.getZoneType() != ZoneType.MAIN) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Ya existe una zona principal activa"
        );
    }

    zone.setName(zoneRequestDTO.getName());
    zone.setDescription(zoneRequestDTO.getDescription());
    zone.setColor(zoneRequestDTO.getColor());
    zone.setIsActive(zoneRequestDTO.getIsActive());
    zone.setZoneType(zoneRequestDTO.getZoneType());
    zone.setGeoJson(zoneRequestDTO.getGeoJson());

    return modelMapper.map(zonesRepository.save(zone), ZoneResponseDTO.class);
}


}
