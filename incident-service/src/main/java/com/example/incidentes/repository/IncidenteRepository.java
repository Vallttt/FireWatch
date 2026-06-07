package com.example.incidentes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.incidentes.enums.ReportStatus;
import com.example.incidentes.model.Incidente;

import java.util.List;
import java.util.UUID;

@Repository
public interface IncidenteRepository extends JpaRepository<Incidente, UUID>{

    List<Incidente> findByEstado(ReportStatus estado);

    List<Incidente> findByReporteId(UUID reporteId);

    void deleteByReporteId(UUID reporteId);

}
