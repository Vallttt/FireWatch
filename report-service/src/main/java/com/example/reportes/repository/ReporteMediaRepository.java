package com.example.reportes.repository;

import com.example.reportes.model.ReporteMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReporteMediaRepository extends JpaRepository<ReporteMedia, UUID> {

    List<ReporteMedia> findByReporteId(UUID reporteId);

    int countByReporteId(UUID reporteId);

    void deleteByReporteId(UUID reporteId);
}
