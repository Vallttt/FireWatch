package cl.duoc.emergency.geo_service.model;

import cl.duoc.emergency.geo_service.enums.ZoneType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "zones")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Zone {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "name",nullable = false)
    private String name;
    @Column(name = "description",nullable = false)
    private String description;
    @Column(name = "color",nullable = false)
    private String color;
    @Enumerated(EnumType.STRING)
    @Column(name = "zone_type",nullable = false)
    private ZoneType zoneType;
    @Column(name = "geojson",nullable = false)
    private String geoJson;
    @Column(name = "is_active",nullable = false)
    private Boolean isActive = true;
    @CreationTimestamp
    @Column(name = "created_at",nullable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at",nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "zone", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<EvacuationRoute> evacuationRoutes = new ArrayList<>();

    @OneToMany(mappedBy = "zone", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Brigade> brigades = new ArrayList<>();

    @OneToMany(mappedBy = "zone", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<MappedReport> mappedReports = new ArrayList<>();
}
