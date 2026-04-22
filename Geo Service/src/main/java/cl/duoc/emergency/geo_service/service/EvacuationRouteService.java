package cl.duoc.emergency.geo_service.service;

import cl.duoc.emergency.geo_service.dto.request.EvacuationRouteRequestDTO;
import cl.duoc.emergency.geo_service.dto.response.EvacuationResponseDTO;
import cl.duoc.emergency.geo_service.model.EvacuationRoute;
import cl.duoc.emergency.geo_service.model.Zone;
import cl.duoc.emergency.geo_service.repository.EvacuationRouteRepository;
import cl.duoc.emergency.geo_service.repository.ZonesRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class EvacuationRouteService {
    private final EvacuationRouteRepository evacuationRouteRepository;
    private final ZonesRepository zonesRepository;
    private final ModelMapper modelMapper;

    public EvacuationResponseDTO createEvacuationRoute(
            EvacuationRouteRequestDTO evacuationRouteRequestDTO){

        Zone zone = zonesRepository.findById(evacuationRouteRequestDTO.getZoneId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "La zona no existe"
                ));

        EvacuationRoute evacuationRoute = new EvacuationRoute();
        evacuationRoute.setName(evacuationRouteRequestDTO.getName());
        evacuationRoute.setDescription(evacuationRouteRequestDTO.getDescription());
        evacuationRoute.setGeoJson(evacuationRouteRequestDTO.getGeoJson());
        evacuationRoute.setZone(zone);

        return mapToResponse(evacuationRouteRepository.save(evacuationRoute));

    }

    public List<EvacuationResponseDTO> findAll(){
        return evacuationRouteRepository.findAll().stream().map(
                EvacuationRoute -> {
                    return modelMapper.map(EvacuationRoute, EvacuationResponseDTO.class);
                }
        ).toList();
    }

    public EvacuationResponseDTO findById(UUID id){
        EvacuationRoute route = evacuationRouteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "La ruta no existe"
                ));


        return mapToResponse(route);
    }

    public EvacuationResponseDTO update(EvacuationRouteRequestDTO evacuationRouteRequestDTO, UUID id){
        EvacuationRoute rute = evacuationRouteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                "La Ruta no existe"
        ));

        Zone zone = zonesRepository.findById(evacuationRouteRequestDTO.getZoneId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "La zona no existe"
                ));

        rute.setName(evacuationRouteRequestDTO.getName());
        rute.setDescription(evacuationRouteRequestDTO.getDescription());
        rute.setGeoJson(evacuationRouteRequestDTO.getGeoJson());
        rute.setZone(zone);


        return mapToResponse(evacuationRouteRepository.save(rute));
    }

    public void deleteById(UUID id) {
        if (!evacuationRouteRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La ruta no existe");
        }

        evacuationRouteRepository.deleteById(id);
    }

    private EvacuationResponseDTO mapToResponse(EvacuationRoute route) {
        EvacuationResponseDTO response = modelMapper.map(route, EvacuationResponseDTO.class);
        response.setZoneId(route.getZone().getId());
        return response;
    }

}
