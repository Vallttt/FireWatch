package com.vallesol.bff.controller;

import com.vallesol.bff.client.UserClient;
import com.vallesol.bff.dtos.request.RegisterRequestDTO;
import com.vallesol.bff.dtos.request.UpdateUserRoleRequestDTO;
import com.vallesol.bff.dtos.request.UpdateUserStatusRequestDTO;
import com.vallesol.bff.dtos.response.UserForAdminResposeDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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

    @GetMapping
    public ResponseEntity<List<UserForAdminResposeDTO>> getAllUsers(){
        return ResponseEntity.ok(userClient.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserForAdminResposeDTO> getUserById(@PathVariable UUID id){
        return ResponseEntity.ok(userClient.getUserById(id));
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<UserForAdminResposeDTO> updateUserStatus(@PathVariable UUID id, @RequestBody UpdateUserStatusRequestDTO requestDTO){
        return ResponseEntity.ok(userClient.udpateUserStatus(id,requestDTO));
    }

    @PutMapping("/role/{id}")
    public ResponseEntity<UserForAdminResposeDTO> updateUserRole(@PathVariable UUID id, @RequestBody UpdateUserRoleRequestDTO requestDTO){
        return ResponseEntity.ok(userClient.udpateUserRole(id,requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable UUID id){
        userClient.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }
}
