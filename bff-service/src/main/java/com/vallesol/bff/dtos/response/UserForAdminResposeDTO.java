package com.vallesol.bff.dtos.response;


import com.vallesol.bff.enums.UserRole;
import com.vallesol.bff.enums.UserStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class UserForAdminResposeDTO {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private UserRole role;
    private UserStatus status;
    private Boolean isActive;
}
