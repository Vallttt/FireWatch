package com.valledelsol.userservice;

import com.valledelsol.userservice.dto.request.RegisterRequestDTO;
import com.valledelsol.userservice.dto.request.ResetPasswordRequest;
import com.valledelsol.userservice.dto.response.RegisterResponseDTO;
import com.valledelsol.userservice.enums.UserRole;
import com.valledelsol.userservice.enums.UserStatus;
import com.valledelsol.userservice.model.User;
import com.valledelsol.userservice.repository.PasswordResetCodeRepository;
import com.valledelsol.userservice.repository.UserRepository;
import com.valledelsol.userservice.service.EmailService;
import com.valledelsol.userservice.service.PasswordResetService;
import com.valledelsol.userservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceApplicationTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetCodeRepository passwordResetCodeRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @Test
    void register_deberiaCrearUsuarioActivoConRolUser() {
        // Arrange
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setFirstName("Juan");
        request.setLastName("Pérez");
        request.setEmail("  JUAN@GMAIL.COM  ");
        request.setPhone("912345678");
        request.setPassword("123456");

        when(userRepository.existsByEmail("juan@gmail.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("123456"))
                .thenReturn("password-codificada");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(UUID.randomUUID());
                    return user;
                });

        // Act
        RegisterResponseDTO response = userService.register(request);

        // Assert
        assertNotNull(response);

        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();

        assertEquals("juan@gmail.com", savedUser.getEmail());
        assertEquals("password-codificada", savedUser.getPasswordHash());
        assertEquals(UserRole.USER, savedUser.getRole());
        assertEquals(UserStatus.ACTIVE, savedUser.getStatus());
        assertTrue(savedUser.getIsActive());
    }

    @Test
    void resetPassword_deberiaRechazarCodigoInvalido() {
        // Arrange
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("usuario@test.cl");
        request.setCode("000000");
        request.setNewPassword("nuevaPassword123");

        when(passwordResetCodeRepository
                .findTopByEmailAndCodeAndUsedFalseOrderByExpiresAtDesc(
                        "usuario@test.cl",
                        "000000"
                ))
                .thenReturn(Optional.empty());

        // Act
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> passwordResetService.resetPassword(request)
        );

        // Assert
        assertEquals(400, exception.getStatusCode().value());
        assertEquals("Codigo invalido", exception.getReason());

        verify(userRepository, never()).save(any());
    }
}