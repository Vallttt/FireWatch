package com.vallesol.bff.dtos.request;

import com.vallesol.bff.enums.BrigadeStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import java.util.UUID;

@Data
public class BrigadeRequestDTO {
    @NotBlank
    private String name;
    @NotBlank
    private String institution;
    @NotNull
    private BrigadeStatus status;
    @NotNull
    @Range(min = -90, max = 90)
    private Double latitude;
    @NotNull
    @Range(min = -180, max = 180)
    private Double longitude;
    @NotNull
    private UUID zoneId; // Opcional — brigada puede no tener zona asignada
}
