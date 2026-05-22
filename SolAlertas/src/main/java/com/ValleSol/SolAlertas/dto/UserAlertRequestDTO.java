package com.ValleSol.SolAlertas.dto;

import lombok.Data;

@Data
public class UserAlertRequestDTO {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
}
