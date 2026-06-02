package com.valledelsol.userservice.controller;


import com.valledelsol.userservice.dto.request.RegisterRequestDTO;
import com.valledelsol.userservice.dto.response.RegisterResponseDTO;
import com.valledelsol.userservice.dto.response.UserAuthResponseDTO;
import com.valledelsol.userservice.dto.response.UserProfileResponseDTO;
import com.valledelsol.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/notificables")
    public ResponseEntity<List<UserProfileResponseDTO>> getUsersForAlerts() {
        return ResponseEntity.ok(userService.findUsersForAlerts());
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
    }

    @GetMapping("/internal")
    public ResponseEntity<UserAuthResponseDTO> findByEmailForAuth(@RequestParam String email) {
        return ResponseEntity.ok(userService.findByEmailForAuth(email));
    }


}
