package com.example.reportes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.reportes.enums.ReportStatus;
import com.example.reportes.model.Reporte;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReporteRepository extends JpaRepository<Reporte, UUID>{

    List<Reporte> findByEstado(ReportStatus estado);

}
