package com.valledelsol.zoneservice.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class EvacuationRouteRequestDTO {
    @NotBlank
    @Size(min = 4, max = 50)
    private String name;
    @NotBlank
    private String description;
    @NotBlank
    @Size(min = 20, max = 50000)
    private String geoJson;
    @NotNull
    private UUID zoneId;
    /** Id del reporte que generó esta ruta (opcional — nulo en rutas legadas). */
    private String reportId;
}
