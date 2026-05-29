package com.vallesol.bff.controller;

import com.vallesol.bff.client.UserClient;
import com.vallesol.bff.dtos.request.RegisterRequestDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserProxyController {

    private final UserClient userClient;

    public UserProxyController(UserClient userClient) {
        this.userClient = userClient;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO registerRequestDTO){
        return ResponseEntity.ok(userClient.register(registerRequestDTO));
    }
}
