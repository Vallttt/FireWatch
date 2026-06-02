package com.ValleSol.notificationservice.service;

import com.ValleSol.notificationservice.dto.UserDTO;
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

    public List<UserDTO> getUsersForAlerts() {
        String url = authServiceUrl + "/api/users/notificables";
        ResponseEntity<UserDTO[]> response = restTemplate.getForEntity(url, UserDTO[].class);
        return Arrays.asList(response.getBody());
    }
}
