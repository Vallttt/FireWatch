package cl.duoc.emergency.geo_service.repository;

import cl.duoc.emergency.geo_service.enums.ZoneType;
import cl.duoc.emergency.geo_service.model.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ZonesRepository extends JpaRepository<Zone, UUID> {

    boolean existsByZoneTypeAndIsActiveTrue(ZoneType zoneType);

    Optional<Zone> findFirstByZoneTypeAndIsActiveTrue(ZoneType zoneType);
}