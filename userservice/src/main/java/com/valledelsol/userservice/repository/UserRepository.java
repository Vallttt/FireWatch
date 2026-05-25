package com.valledelsol.userservice.repository;


import com.valledelsol.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID; //el tipo de dato UUID es para el id del usuario, es un identificador único universal

public interface UserRepository extends JpaRepository<User, UUID> { 
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE' AND (u.email IS NOT NULL OR u.phone IS NOT NULL)") //esta consulta JPQL selecciona a los usuarios activos que tienen un correo electrónico o un número de teléfono registrado, lo que es útil para enviar alertas.
    List<User> findUsersForAlerts();
}