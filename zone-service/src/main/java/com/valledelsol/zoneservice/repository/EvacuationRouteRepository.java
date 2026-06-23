package com.valledelsol.zoneservice.repository;


import com.valledelsol.zoneservice.model.EvacuationRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EvacuationRouteRepository extends JpaRepository<EvacuationRoute, UUID> {

    List<EvacuationRoute> findByIsActiveTrue();

    List<EvacuationRoute> findByZoneIdAndIsActiveTrue(UUID zoneId);

    List<EvacuationRoute> findByReportIdAndIsActiveTrue(String reportId);
}
