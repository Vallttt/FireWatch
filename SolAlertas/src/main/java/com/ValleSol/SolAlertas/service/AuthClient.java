package com.ValleSol.SolAlertas.service;

import com.ValleSol.SolAlertas.dto.UserAlertRequestDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class AuthClient {

    @Value("${auth.service.url}")
    private String authServiceUrl;

    private final RestTemplate restTemplate;

    public AuthClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<UserAlertRequestDTO> getUsersForAlerts() {
        String url = authServiceUrl + "/api/users/notificables";

        ResponseEntity<UserAlertRequestDTO[]> response =
                restTemplate.getForEntity(url, UserAlertRequestDTO[].class);

        return Arrays.asList(response.getBody());
    }
}
