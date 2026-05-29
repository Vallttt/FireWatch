package com.privdata.authservice.dto.response;

import com.privdata.authservice.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDTO {

    private String token;
    private String type;
    private UUID userId;
    private String email;
    private UserRole role;
}