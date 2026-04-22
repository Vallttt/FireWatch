package cl.duoc.emergency.geo_service.repository;

import cl.duoc.emergency.geo_service.model.Brigade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BrigadeRepository extends JpaRepository<Brigade, UUID> {
}
