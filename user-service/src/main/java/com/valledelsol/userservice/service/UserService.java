package com.valledelsol.userservice.service;


import com.valledelsol.userservice.dto.request.RegisterRequestDTO;
import com.valledelsol.userservice.dto.request.UpdateUserRoleRequestDTO;
import com.valledelsol.userservice.dto.request.UpdateUserStatusRequestDTO;
import com.valledelsol.userservice.dto.response.RegisterResponseDTO;
import com.valledelsol.userservice.dto.response.UserAuthResponseDTO;
import com.valledelsol.userservice.dto.response.UserForAdminResposeDTO;
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
import java.util.UUID;

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
        user.setIsActive(true);

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

    public List<UserForAdminResposeDTO> findAllUsers(){

        List<User> users = userRepository.findAll();

        if (users.isEmpty()){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No existen usuarios registrados"
            );
        }

        return users.stream()
                .map(user -> modelMapper.map(user, UserForAdminResposeDTO.class))
                .toList();
    }

    public UserForAdminResposeDTO findUserById(UUID id){

        User user = userRepository.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Usuario no encontrado"
                        ));

        return modelMapper.map(user,UserForAdminResposeDTO.class);
    }

    public UserForAdminResposeDTO updateStatus(UUID id, UpdateUserStatusRequestDTO requestDTO){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"
                ));

        if (requestDTO.getStatus() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El estado no puede ser nulo"
            );
        }

        user.setStatus(requestDTO.getStatus());

        if (requestDTO.getStatus() == UserStatus.ACTIVE) {
            user.setIsActive(true);
        } else if (requestDTO.getStatus() == UserStatus.INACTIVE) {
            user.setIsActive(false);
        }

        return modelMapper.map(
                userRepository.save(user),
                UserForAdminResposeDTO.class
        );
    }

    public UserForAdminResposeDTO updateRol(UUID id, UpdateUserRoleRequestDTO requestDTO){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"
                ));

        System.out.println("=== DEBUG UPDATE ROLE ===");
        System.out.println("ID: " + id);
        System.out.println("ROLE RECIBIDO: " + requestDTO.getRole());
        System.out.println("IS ACTIVE: " + user.getIsActive());
        System.out.println("STATUS: " + user.getStatus());

        if (requestDTO.getRole() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El rol no puede ser nulo"
            );
        }

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El usuario se encuentra inactivo"
            );
        }
        user.setRole(requestDTO.getRole());


        return modelMapper.map(userRepository.save(user), UserForAdminResposeDTO.class);
    }

    public void deleteById(UUID id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"
                ));

        user.setIsActive(false);
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
    }
}


