package com.ValleSol.alertservice.repository;

import com.ValleSol.alertservice.model.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AlertaRepository extends JpaRepository<Alerta, UUID> {
}
