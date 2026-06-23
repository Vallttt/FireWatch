package com.privdata.authservice.controller;

import com.privdata.authservice.dto.request.LoginRequestDTO;
import com.privdata.authservice.dto.request.RefreshTokenRequestDTO;
import com.privdata.authservice.dto.response.LoginResponseDTO;
import com.privdata.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * ✅ LOGIN: Devolver token y refresh token
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        log.info("Login attempt for email: {}", request.getEmail());
        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * ✅ REFRESH TOKEN: Usar refresh token para obtener un nuevo access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refreshToken(
            @Valid @RequestBody RefreshTokenRequestDTO request) {

        log.debug("Refresh token request received");

        try {
            LoginResponseDTO response = authService.refreshToken(request.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid refresh token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null);
        }
    }
}