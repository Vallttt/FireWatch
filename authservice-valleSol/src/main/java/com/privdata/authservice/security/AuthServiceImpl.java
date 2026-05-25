package com.privdata.authservice.security;

import com.privdata.authservice.client.UserClient;
import com.privdata.authservice.dto.request.LoginRequestDTO;
import com.privdata.authservice.dto.response.LoginResponseDTO;
import com.privdata.authservice.dto.response.UserAuthResponseDTO;
import com.privdata.authservice.enums.UserStatus;
import com.privdata.authservice.model.SecurityUser;
import com.privdata.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserClient userClient;


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

        return new LoginResponseDTO(
                jwtToken,
                "Bearer",
                user.getId(),
                user.getEmail(),
                user.getRole()
        );
    }
}