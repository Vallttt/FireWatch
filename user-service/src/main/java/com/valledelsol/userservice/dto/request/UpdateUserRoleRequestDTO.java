package com.valledelsol.userservice.dto.request;

import com.valledelsol.userservice.enums.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserRoleRequestDTO {
    @NotNull
    private UserRole role;
}
