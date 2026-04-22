package cl.duoc.emergency.geo_service.service;

import cl.duoc.emergency.geo_service.dto.request.ZoneRequestDTO;
import cl.duoc.emergency.geo_service.dto.response.ZoneResponseDTO;
import cl.duoc.emergency.geo_service.model.Zone;
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
public class ZonesService {
    private final ZonesRepository zonesRepository;
    private final ModelMapper modelMapper;

    public ZoneResponseDTO createZone(ZoneRequestDTO zoneRequestDTO){
        Zone zone = new Zone();
        zone.setName(zoneRequestDTO.getName());
        zone.setDescription(zoneRequestDTO.getDescription());
        zone.setColor(zoneRequestDTO.getColor());
        zone.setZoneType(zoneRequestDTO.getZoneType());
        zone.setGeoJson(zoneRequestDTO.getGeoJson());
        zone.setIsActive(true);

        return modelMapper.map(zonesRepository.save(zone), ZoneResponseDTO.class);
    }

    public List<ZoneResponseDTO> findAll(){
        return zonesRepository.findAll().stream().map(
                zone -> {
                    return modelMapper.map(zone, ZoneResponseDTO.class);
                }
                ).toList();
    }

    public ZoneResponseDTO findById(UUID id){
        Optional<Zone> optionalZone = zonesRepository.findById(id);
        if (optionalZone.isEmpty()){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"La zona no existe");
        }

        Zone zone = optionalZone.get();

        return modelMapper.map(zone, ZoneResponseDTO.class);

    }

    public void deleteById(UUID id){
        if (!zonesRepository.existsById(id)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"La zona no existe");
        }
        zonesRepository.deleteById(id);

    }

    public ZoneResponseDTO updateZone(ZoneRequestDTO zoneRequestDTO, UUID id) {
        Zone zone = zonesRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "La zona no existe"
                ));

        zone.setName(zoneRequestDTO.getName());
        zone.setDescription(zoneRequestDTO.getDescription());
        zone.setColor(zoneRequestDTO.getColor());
        zone.setIsActive(zoneRequestDTO.getIsActive());
        zone.setZoneType(zoneRequestDTO.getZoneType());
        zone.setGeoJson(zoneRequestDTO.getGeoJson());


        return modelMapper.map(zonesRepository.save(zone), ZoneResponseDTO.class);
    }


}
