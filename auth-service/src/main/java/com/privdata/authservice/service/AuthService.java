package com.privdata.authservice.service;

import com.privdata.authservice.dto.request.LoginRequestDTO;
import com.privdata.authservice.dto.response.LoginResponseDTO;

public interface AuthService {

    /**
     * ✅ Login: autenticar usuario y devolver tokens
     */
    LoginResponseDTO login(LoginRequestDTO request);

    /**
     * ✅ Refresh token: obtener nuevo access token usando refresh token
     */
    LoginResponseDTO refreshToken(String refreshToken);
}