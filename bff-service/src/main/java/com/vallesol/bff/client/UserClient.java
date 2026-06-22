package com.vallesol.bff.client;

import com.vallesol.bff.dtos.request.UpdateUserRoleRequestDTO;
import com.vallesol.bff.dtos.request.UpdateUserStatusRequestDTO;
import com.vallesol.bff.dtos.response.ApiResponseDTO;
import com.vallesol.bff.dtos.response.UserForAdminResposeDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

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

    public List<UserForAdminResposeDTO> getAllUsers(){

        ApiResponseDTO<List<UserForAdminResposeDTO>> responseDTO =
            restClient.get()
                    .uri(userServiceUrl + "/api/users")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

        return responseDTO.getData();
    }

    public UserForAdminResposeDTO getUserById(UUID id){

        ApiResponseDTO<UserForAdminResposeDTO> responseDTO =
                restClient.get()
                        .uri(userServiceUrl + "/api/users/" + id)
                        .retrieve()
                        .body(new ParameterizedTypeReference<>() {});

        return responseDTO.getData();
    }

    public UserForAdminResposeDTO udpateUserStatus(UUID id, UpdateUserStatusRequestDTO requestDTO){

        ApiResponseDTO<UserForAdminResposeDTO> responseDTO =
                restClient.put()
                        .uri(userServiceUrl + "/api/users/status/" + id)
                        .body(requestDTO)
                        .retrieve()
                        .body(new ParameterizedTypeReference<>() {});

        return responseDTO.getData();
    }

    public UserForAdminResposeDTO udpateUserRole(UUID id, UpdateUserRoleRequestDTO requestDTO){

        ApiResponseDTO<UserForAdminResposeDTO> responseDTO =
                restClient.put()
                        .uri(userServiceUrl + "/api/users/role/" + id)
                        .body(requestDTO)
                        .retrieve()
                        .body(new ParameterizedTypeReference<>() {});

        return responseDTO.getData();
    }

    public void deleteUserById(UUID id){
        restClient.delete()
                .uri(userServiceUrl + "/api/users/" + id)
                .retrieve()
                .toBodilessEntity();
    }

    public Object forgotPassword(Object request) {
    return restClient.post()
            .uri(userServiceUrl + "/api/auth/password/forgot")
            .body(request)
            .retrieve()
            .body(Object.class);
}

    public Object resetPassword(Object request) {
        return restClient.post()
                .uri(userServiceUrl + "/api/auth/password/reset")
                .body(request)
                .retrieve()
                .body(Object.class);
    }
}
