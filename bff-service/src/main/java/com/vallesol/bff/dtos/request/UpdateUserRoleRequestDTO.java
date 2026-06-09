package com.vallesol.bff.dtos.request;

import com.vallesol.bff.enums.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserRoleRequestDTO {
    @NotNull
    private UserRole role;
}
