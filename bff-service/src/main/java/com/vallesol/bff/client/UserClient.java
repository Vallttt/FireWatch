package com.vallesol.bff.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class UserClient {

    @Value("${services.user.url}")
    private String userServiceUrl;


    private final RestClient restClient;


    public UserClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public Object register(Object request) {
        return restClient.post()
                .uri(userServiceUrl + "/api/users/register")
                .body(request)
                .retrieve()
                .body(Object.class);
    }
}
