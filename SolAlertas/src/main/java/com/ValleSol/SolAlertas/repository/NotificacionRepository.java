package com.ValleSol.SolAlertas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ValleSol.SolAlertas.model.Notificacion;

@Repository
public interface NotificacionRepository extends JpaRepository <Notificacion,Long> {

}
