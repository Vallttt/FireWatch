package com.valledelsol.userservice.controller;

import com.valledelsol.userservice.dto.request.RegisterRequestDTO;
import com.valledelsol.userservice.dto.request.UpdateUserRoleRequestDTO;
import com.valledelsol.userservice.dto.request.UpdateUserStatusRequestDTO;
import com.valledelsol.userservice.dto.response.RegisterResponseDTO;
import com.valledelsol.userservice.dto.response.UserAuthResponseDTO;
import com.valledelsol.userservice.dto.response.UserForAdminResposeDTO;
import com.valledelsol.userservice.dto.response.UserProfileResponseDTO;
import com.valledelsol.userservice.service.UserService;
import com.valledelsol.userservice.shared.ApiResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/notificables")
    public ResponseEntity<ApiResponseDTO<List<UserProfileResponseDTO>>> getUsersForAlerts() {

        List<UserProfileResponseDTO> users = userService.findUsersForAlerts();

        ApiResponseDTO<List<UserProfileResponseDTO>> responseDTO =
                new ApiResponseDTO<>(true, "Usuarios notificables obtenidos correctamente", users);

        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponseDTO<RegisterResponseDTO>> register(
            @Valid @RequestBody RegisterRequestDTO request) {

        RegisterResponseDTO user = userService.register(request);

        ApiResponseDTO<RegisterResponseDTO> responseDTO =
                new ApiResponseDTO<>(true, "Usuario registrado correctamente", user);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping("/internal")
    public ResponseEntity<ApiResponseDTO<UserAuthResponseDTO>> findByEmailForAuth(
            @RequestParam String email) {

        UserAuthResponseDTO user = userService.findByEmailForAuth(email);

        ApiResponseDTO<UserAuthResponseDTO> responseDTO =
                new ApiResponseDTO<>(true, "Usuario encontrado correctamente", user);

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<UserForAdminResposeDTO>>> findAllUsers() {

        List<UserForAdminResposeDTO> users = userService.findAllUsers();

        ApiResponseDTO<List<UserForAdminResposeDTO>> responseDTO =
                new ApiResponseDTO<>(true, "Lista de usuarios obtenida correctamente", users);

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<UserForAdminResposeDTO>> findUserById(
            @PathVariable UUID id) {

        UserForAdminResposeDTO user = userService.findUserById(id);

        ApiResponseDTO<UserForAdminResposeDTO> responseDTO =
                new ApiResponseDTO<>(true, "Usuario encontrado correctamente", user);

        return ResponseEntity.ok(responseDTO);
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<ApiResponseDTO<UserForAdminResposeDTO>> updateUserStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserStatusRequestDTO requestDTO) {

        UserForAdminResposeDTO user = userService.updateStatus(id, requestDTO);

        ApiResponseDTO<UserForAdminResposeDTO> responseDTO =
                new ApiResponseDTO<>(true, "Estado del usuario actualizado correctamente", user);

        return ResponseEntity.ok(responseDTO);
    }

    @PutMapping("/role/{id}")
    public ResponseEntity<ApiResponseDTO<UserForAdminResposeDTO>> updateUserRole(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRoleRequestDTO requestDTO) {

        UserForAdminResposeDTO user = userService.updateRol(id, requestDTO);

        ApiResponseDTO<UserForAdminResposeDTO> responseDTO =
                new ApiResponseDTO<>(true, "Rol del usuario actualizado correctamente", user);

        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteById(@PathVariable UUID id) {

        userService.deleteById(id);

        ApiResponseDTO<Void> responseDTO =
                new ApiResponseDTO<>(true, "Usuario eliminado correctamente", null);

        return ResponseEntity.ok(responseDTO);
    }
}