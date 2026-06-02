package com.valledelsol.userservice.config;

import com.valledelsol.userservice.enums.UserRole;
import com.valledelsol.userservice.enums.UserStatus;
import com.valledelsol.userservice.model.User;
import com.valledelsol.userservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {

        System.out.println("=== DATA INITIALIZER EJECUTANDO ===");

        createUserIfNotExists(
                "Admin",
                "FireWatch",
                "admin@valledelsol.cl",
                "Admin123*",
                "+56911111111",
                UserRole.ADMIN
        );

        createUserIfNotExists(
                "Usuario",
                "Demo",
                "w.vinet.h@gmail.com",
                "User123*",
                "+56922222222",
                UserRole.USER
        );
    }

    private void createUserIfNotExists(
            String firstName,
            String lastName,
            String email,
            String rawPassword,
            String phone,
            UserRole role
    ) {

        User user = userRepository.findByEmail(email)
                .orElse(new User());


        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setPhone(phone);
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(role);

        userRepository.save(user);
    }
}