package com.valledelsol.userservice.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class UserAlertResponseDTO {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
}
