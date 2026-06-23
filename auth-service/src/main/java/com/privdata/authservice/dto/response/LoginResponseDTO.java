package com.privdata.authservice.dto.response;

import com.privdata.authservice.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponseDTO {

    private String token;
    private String refreshToken;    // ✅ Nuevo: refresh token
    private String type;
    private UUID userId;
    private String email;
    private UserRole role;
    private Long expiresIn;         // ✅ Nuevo: tiempo de expiración en segundos
}