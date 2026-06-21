package com.privdata.authservice.client;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mirrors user-service's response envelope ({success, message, data}) so
 * Feign/RestClient can correctly unwrap responses from /api/users/**.
 */
@Data
@NoArgsConstructor
public class ApiResponseDTO<T> {
    private boolean success;
    private String message;
    private T data;
}
