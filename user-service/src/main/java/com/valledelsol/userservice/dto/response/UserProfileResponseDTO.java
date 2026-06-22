package com.valledelsol.userservice.dto.response;

import lombok.Data;

@Data
public class UserProfileResponseDTO {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String role;

}
