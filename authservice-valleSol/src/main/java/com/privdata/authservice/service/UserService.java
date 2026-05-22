package com.privdata.authservice.service;

import com.privdata.authservice.dto.response.UserProfileResponseDTO;
import com.privdata.authservice.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public List<UserProfileResponseDTO> findUsersForAlerts() {
        return userRepository.findUsersForAlerts()
                .stream()
                .map(user -> modelMapper.map(user, UserProfileResponseDTO.class))
                .toList();
    }
}
