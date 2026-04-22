package cl.duoc.emergency.geo_service.model;

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
    @Column(name = "geojson",nullable = false)
    private String geoJson;
    @Column(name = "is_active",nullable = false)
    private boolean isActive;
    @CreationTimestamp
    @Column(name = "created_at",nullable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at",nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)//no trae la zona automaticamente, solo cuando se solicite
    @JoinColumn(name = "zone_id", nullable = false)
    @JsonIgnoreProperties({"evacuationRoutes", "brigades", "mappedReports"})
    private Zone zone;

}
