package com.valledelsol.userservice.repository;

import com.valledelsol.userservice.model.PasswordResetCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, UUID> {

    Optional<PasswordResetCode> findTopByEmailAndCodeAndUsedFalseOrderByExpiresAtDesc(
            String email,
            String code
    );

    void deleteByEmail(String email);
}
