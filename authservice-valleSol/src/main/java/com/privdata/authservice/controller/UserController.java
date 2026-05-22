package com.privdata.authservice.controller;

import com.privdata.authservice.dto.response.UserProfileResponseDTO;
import com.privdata.authservice.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
