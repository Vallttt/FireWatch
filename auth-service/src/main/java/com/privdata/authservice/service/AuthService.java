package com.privdata.authservice.service;

import com.privdata.authservice.dto.request.LoginRequestDTO;
import com.privdata.authservice.dto.response.LoginResponseDTO;

public interface AuthService {

    LoginResponseDTO login(LoginRequestDTO request);
}