package com.valledelsol.zoneservice.repository;


import com.valledelsol.zoneservice.enums.ZoneType;
import com.valledelsol.zoneservice.model.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ZonesRepository extends JpaRepository<Zone, UUID> {

    boolean existsByZoneTypeAndIsActiveTrue(ZoneType zoneType);

    Optional<Zone> findFirstByZoneTypeAndIsActiveTrue(ZoneType zoneType);
}