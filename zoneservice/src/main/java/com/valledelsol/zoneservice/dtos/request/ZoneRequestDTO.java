package com.valledelsol.zoneservice.dtos.request;


import com.valledelsol.zoneservice.enums.ZoneType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ZoneRequestDTO {
    @NotBlank
    @Size(min = 4, max = 50)
    private String name;
    @NotBlank
    @Size( max = 250)
    private String description;
    @NotNull
    private Boolean isActive;
    @NotBlank
    @Size(min = 7, max = 7)
    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$")
    private String color;
    @NotNull
    private ZoneType zoneType;
    @NotBlank
    @Size(min = 20, max = 10000)
    private String geoJson;
}
