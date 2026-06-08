package com.valledelsol.userservice.dto.request;

import com.valledelsol.userservice.enums.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserStatusRequestDTO {
    @NotNull
    private UserStatus status;
}
