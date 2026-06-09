package com.vallesol.bff.dtos.request;

import com.vallesol.bff.enums.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserStatusRequestDTO {
    @NotNull
    private UserStatus status;
}
