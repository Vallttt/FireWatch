package com.privdata.authservice.dto.response;

import com.privdata.authservice.enums.UserRole;
import com.privdata.authservice.enums.UserStatus;
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
