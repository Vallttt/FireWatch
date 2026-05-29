package com.privdata.authservice.client;

import com.privdata.authservice.dto.response.UserAuthResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class UserClient {

    private final RestClient restClient;

    @Value("${services.user.url}")
    private String userServiceUrl;

    public UserAuthResponseDTO findByEmail(String email) {
        return restClient.get()
                .uri(userServiceUrl + "/api/users/internal?email=" + email)
                .retrieve()
                .body(UserAuthResponseDTO.class);
    }
}
