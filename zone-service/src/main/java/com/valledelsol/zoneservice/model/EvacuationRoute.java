package com.valledelsol.zoneservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "evacuation_routes")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EvacuationRoute {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "name",nullable = false)
    private String name;
    @Column(name = "description",nullable = false)
    private String description;
    @Column(name = "geojson", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String geoJson;
    @Column(name = "is_active",nullable = false)
    private boolean isActive;
    /** Id del reporte (report-service) que generó esta ruta. Permite mostrarla
     *  solo al ciudadano dueño de ese reporte, y borrarla si el reporte se
     *  elimina o el incendio se marca finalizado. */
    @Column(name = "report_id")
    private String reportId;
    @CreationTimestamp
    @Column(name = "created_at",nullable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at",nullable = false)
    private LocalDateTime updatedAt;
    @ManyToOne
    @JoinColumn(name = "zone_id")
    private Zone zone;

}
