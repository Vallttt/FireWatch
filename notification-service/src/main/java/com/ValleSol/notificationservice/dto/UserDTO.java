package com.ValleSol.notificationservice.dto;

import lombok.Data;


@Data
public class UserDTO {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    private String role;
}
