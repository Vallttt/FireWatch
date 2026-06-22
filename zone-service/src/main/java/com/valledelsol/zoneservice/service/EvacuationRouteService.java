package com.valledelsol.zoneservice.service;


import com.valledelsol.zoneservice.dtos.request.EvacuationRouteRequestDTO;
import com.valledelsol.zoneservice.dtos.response.EvacuationResponseDTO;
import com.valledelsol.zoneservice.enums.ZoneType;
import com.valledelsol.zoneservice.model.EvacuationRoute;
import com.valledelsol.zoneservice.model.Zone;
import com.valledelsol.zoneservice.repository.EvacuationRouteRepository;
import com.valledelsol.zoneservice.repository.ZonesRepository;
import lombok.AllArgsConstructor;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class EvacuationRouteService {
    private final EvacuationRouteRepository evacuationRouteRepository;
    private final ZonesRepository zonesRepository;
    private final ModelMapper modelMapper;

    public EvacuationResponseDTO createEvacuationRoute(
            EvacuationRouteRequestDTO evacuationRouteRequestDTO){

        Zone zone = zoneExistsValidation(evacuationRouteRequestDTO);

        if (zone.getZoneType() != ZoneType.OPERATIONAL) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Las rutas solo pueden asignarse a zonas operativas"
            );
        }

        Geometry zoneGeometry = geoJsonToGeometry(zone.getGeoJson());
        Geometry routeGeometry = geoJsonToGeometry(evacuationRouteRequestDTO.getGeoJson());

        if (!"LineString".equalsIgnoreCase(routeGeometry.getGeometryType())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La ruta debe ser un GeoJSON de tipo LineString"
            );
        }

        if (!zoneGeometry.intersects(routeGeometry)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La ruta debe partir desde la zona asignada"
            );
        }


        EvacuationRoute evacuationRoute = new EvacuationRoute();
        evacuationRoute.setName(evacuationRouteRequestDTO.getName());
        evacuationRoute.setDescription(evacuationRouteRequestDTO.getDescription());
        evacuationRoute.setGeoJson(evacuationRouteRequestDTO.getGeoJson());
        evacuationRoute.setZone(zone);
        evacuationRoute.setActive(true);

        return mapToResponse(evacuationRouteRepository.save(evacuationRoute));

    }

    public List<EvacuationResponseDTO> findAll() {
        return evacuationRouteRepository.findByIsActiveTrue()
                .stream()
                .map(evacuationRoute -> modelMapper.map(evacuationRoute, EvacuationResponseDTO.class))
                .toList();
    }

    public EvacuationResponseDTO findById(UUID id){
        EvacuationRoute route = evacuationRouteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "La ruta no existe"
                ));

        if (!route.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "La ruta no existe o está inactiva"
            );
        }


        return mapToResponse(route);
    }

    public List<EvacuationResponseDTO> findByZoneId(UUID zoneId) {
        return evacuationRouteRepository.findByZoneIdAndIsActiveTrue(zoneId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public EvacuationResponseDTO update(EvacuationRouteRequestDTO evacuationRouteRequestDTO, UUID id){
        EvacuationRoute rute = evacuationRouteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                "La Ruta no existe"
        ));

        Zone zone = zoneExistsValidation(evacuationRouteRequestDTO);

        if (zone.getZoneType() != ZoneType.OPERATIONAL) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Las rutas solo pueden asignarse a zonas operativas"
            );
        }

        Geometry zoneGeometry = geoJsonToGeometry(zone.getGeoJson());
        Geometry routeGeometry = geoJsonToGeometry(evacuationRouteRequestDTO.getGeoJson());

        if (!"LineString".equalsIgnoreCase(routeGeometry.getGeometryType())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La ruta debe ser un GeoJSON de tipo LineString"
            );
        }

        if (!zoneGeometry.intersects(routeGeometry)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La ruta debe partir desde la zona asignada"
            );
        }

        rute.setName(evacuationRouteRequestDTO.getName());
        rute.setDescription(evacuationRouteRequestDTO.getDescription());
        rute.setGeoJson(evacuationRouteRequestDTO.getGeoJson());
        rute.setZone(zone);


        return mapToResponse(evacuationRouteRepository.save(rute));
    }

    public void deleteById(UUID id) {

        EvacuationRoute route = evacuationRouteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "La ruta no existe"
                ));

        route.setActive(false);
        evacuationRouteRepository.save(route);
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

    private EvacuationResponseDTO mapToResponse(EvacuationRoute route) {

        return modelMapper.map(route, EvacuationResponseDTO.class);
    }

    private Zone zoneExistsValidation(EvacuationRouteRequestDTO requestDTO){

        return zonesRepository.findById(requestDTO.getZoneId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "La zona no existe"
                ));
    }



}
