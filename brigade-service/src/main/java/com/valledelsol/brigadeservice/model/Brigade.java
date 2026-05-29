package com.valledelsol.brigadeservice.model;

import com.valledelsol.brigadeservice.enums.BrigadeStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "brigades")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Brigade {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "institution", nullable = false)
    private String institution;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BrigadeStatus status;
    @Column(name = "latitude", nullable = false)
    private Double latitude;
    @Column(name = "longitude", nullable = false)
    private Double longitude;
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    @Column(name = "zone_id")
    private UUID zoneId;
}
