package com.privdata.authservice;

import com.privdata.authservice.client.UserClient;
import com.privdata.authservice.dto.request.LoginRequestDTO;
import com.privdata.authservice.dto.response.LoginResponseDTO;
import com.privdata.authservice.dto.response.UserAuthResponseDTO;
import com.privdata.authservice.enums.UserRole;
import com.privdata.authservice.enums.UserStatus;
import com.privdata.authservice.security.AuthServiceImpl;
import com.privdata.authservice.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthserviceApplicationTests {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserAuthResponseDTO activeUser;

    @BeforeEach
    void setUp() {
        activeUser = new UserAuthResponseDTO(
                UUID.randomUUID(),
                "usuario@test.cl",
                "password-hash",
                UserRole.USER,
                UserStatus.ACTIVE
        );

        ReflectionTestUtils.setField(
                authService,
                "jwtExpiration",
                86400000L
        );
    }

    @Test
    void login_deberiaRetornarTokenCuandoCredencialesSonCorrectas() {
        // Arrange
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("  USUARIO@TEST.CL  ");
        request.setPassword("123456");

        when(userClient.findByEmail("usuario@test.cl"))
                .thenReturn(activeUser);

        when(passwordEncoder.matches(
                "123456",
                "password-hash"
        )).thenReturn(true);

        when(jwtService.generateToken(any()))
                .thenReturn("token-jwt");

        // Act
        LoginResponseDTO response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("token-jwt", response.getToken());
        assertEquals("Bearer", response.getType());
        assertEquals("usuario@test.cl", response.getEmail());
        assertEquals(UserRole.USER, response.getRole());

        verify(userClient).findByEmail("usuario@test.cl");
        verify(jwtService).generateToken(any());
    }

    @Test
    void login_deberiaRechazarContrasenaIncorrecta() {
        // Arrange
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("usuario@test.cl");
        request.setPassword("incorrecta");

        when(userClient.findByEmail("usuario@test.cl"))
                .thenReturn(activeUser);

        when(passwordEncoder.matches(
                "incorrecta",
                "password-hash"
        )).thenReturn(false);

        // Act
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(request)
        );

        // Assert
        assertEquals(401, exception.getStatusCode().value());
        assertEquals("Credenciales inválidas", exception.getReason());

        verify(jwtService, never()).generateToken(any());
    }
}