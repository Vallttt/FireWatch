package com.valledelsol.brigadeservice.repository;

import com.valledelsol.brigadeservice.model.Brigade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BrigadeRepository extends JpaRepository<Brigade, UUID> {
}
