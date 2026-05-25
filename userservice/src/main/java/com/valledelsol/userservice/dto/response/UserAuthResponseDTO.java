package com.valledelsol.userservice.dto.response;

import com.valledelsol.userservice.enums.UserRole;
import com.valledelsol.userservice.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserAuthResponseDTO {

    private UUID id;
    private String email;
    private String passwordHash;
    private UserRole role;
    private UserStatus status;
}
