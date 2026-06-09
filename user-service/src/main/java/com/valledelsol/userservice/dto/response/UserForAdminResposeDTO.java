package com.valledelsol.userservice.dto.response;

import com.valledelsol.userservice.enums.UserRole;
import com.valledelsol.userservice.enums.UserStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class UserForAdminResposeDTO {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private UserRole role;
    private UserStatus status;
    private Boolean isActive;
}
