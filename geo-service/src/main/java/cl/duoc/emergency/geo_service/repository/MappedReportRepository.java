package cl.duoc.emergency.geo_service.repository;

import cl.duoc.emergency.geo_service.model.MappedReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MappedReportRepository extends JpaRepository<MappedReport, UUID> {
}
