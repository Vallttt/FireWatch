package com.ValleSol.notificationservice.service;

import com.ValleSol.notificationservice.dto.UserDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
public class UserClient {

    @Value("${user.service.url}")
    private String userServiceUrl;

    private final RestTemplate restTemplate;

    public UserClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<UserDTO> getUsersForAlerts() {
        String url = userServiceUrl + "/api/users/notificables";

        UserApiResponse response = restTemplate.getForObject(url, UserApiResponse.class);

        if (response == null || response.getData() == null) {
            return Collections.emptyList();
        }

        return response.getData();
    }

    public static class UserApiResponse {
        private boolean success;
        private String message;
        private List<UserDTO> data;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public List<UserDTO> getData() {
            return data;
        }

        public void setData(List<UserDTO> data) {
            this.data = data;
        }
    }
}