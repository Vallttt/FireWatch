package com.privdata.authservice.security;

import com.privdata.authservice.client.UserClient;
import com.privdata.authservice.dto.request.LoginRequestDTO;
import com.privdata.authservice.dto.response.LoginResponseDTO;
import com.privdata.authservice.dto.response.UserAuthResponseDTO;
import com.privdata.authservice.enums.UserStatus;
import com.privdata.authservice.model.SecurityUser;
import com.privdata.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserClient userClient;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    /**
     * ✅ LOGIN: Autenticar usuario y devolver access token + refresh token
     */
    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {

        String email = request.getEmail().trim().toLowerCase();

        UserAuthResponseDTO user = userClient.findByEmail(email);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Credenciales inválidas"
            );
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Usuario no habilitado para iniciar sesión"
            );
        }

        SecurityUser securityUser = new SecurityUser(user);
        String jwtToken = jwtService.generateToken(securityUser);

        // ✅ Refresh token es igual al access token (mismo JWT)
        // En producción, podrías tener un refresh token separado con mayor expiración
        String refreshToken = jwtToken;

        // ✅ Calcular expiración en segundos
        Long expiresIn = jwtExpiration / 1000;

        return LoginResponseDTO.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .type("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .expiresIn(expiresIn)
                .build();
    }

    /**
     * ✅ REFRESH TOKEN: Obtener nuevo access token
     */
    @Override
    public LoginResponseDTO refreshToken(String refreshToken) {

        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new IllegalArgumentException("Refresh token no puede estar vacío");
        }

        try {
            // ✅ Validar que el refresh token sea válido
            String email = jwtService.extractUsername(refreshToken);

            if (email == null || email.isEmpty()) {
                throw new IllegalArgumentException("Token inválido");
            }

            // ✅ Obtener el usuario
            UserAuthResponseDTO user = userClient.findByEmail(email);

            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Usuario no habilitado"
                );
            }

            // ✅ Generar nuevo access token
            SecurityUser securityUser = new SecurityUser(user);
            String newAccessToken = jwtService.generateToken(securityUser);

            Long expiresIn = jwtExpiration / 1000;

            log.info("✅ Refresh token exitoso para usuario: {}", email);

            return LoginResponseDTO.builder()
                    .token(newAccessToken)
                    .refreshToken(newAccessToken)
                    .type("Bearer")
                    .userId(user.getId())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .expiresIn(expiresIn)
                    .build();

        } catch (Exception e) {
            log.error("❌ Error refrescando token: {}", e.getMessage());
            throw new IllegalArgumentException("Token de refresh inválido o expirado", e);
        }
    }
}