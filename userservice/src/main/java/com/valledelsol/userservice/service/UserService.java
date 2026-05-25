package com.valledelsol.userservice.service;


import com.valledelsol.userservice.dto.request.RegisterRequestDTO;
import com.valledelsol.userservice.dto.response.RegisterResponseDTO;
import com.valledelsol.userservice.dto.response.UserAuthResponseDTO;
import com.valledelsol.userservice.dto.response.UserProfileResponseDTO;
import com.valledelsol.userservice.enums.UserRole;
import com.valledelsol.userservice.enums.UserStatus;
import com.valledelsol.userservice.model.User;
import com.valledelsol.userservice.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    public RegisterResponseDTO register(RegisterRequestDTO request) {

        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El correo ya se encuentra registrado"
            );
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(email);
        user.setPhone(request.getPhone());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatus.ACTIVE);

        // @vallesol.cl → Administrador municipal automáticamente
        if (email.endsWith("@vallesol.cl")) {
            user.setRole(UserRole.ADMIN);
        } else {
            user.setRole(UserRole.USER);
        }

        User savedUser = userRepository.save(user);

        return new RegisterResponseDTO(
                savedUser.getId(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getEmail(),
                savedUser.getPhone(),
                savedUser.getStatus(),
                savedUser.getRole()
        );
    }

    public List<UserProfileResponseDTO> findUsersForAlerts() {
        return userRepository.findUsersForAlerts()
                .stream()
                .map(user -> modelMapper.map(user, UserProfileResponseDTO.class))
                .toList();
    }

    public UserAuthResponseDTO findByEmailForAuth(String email) {

        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"
                ));

        return new UserAuthResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getRole(),
                user.getStatus()
        );
    }
}
