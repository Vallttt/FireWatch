package com.ValleSol.SolAlertas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ValleSol.SolAlertas.model.Notificacion;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, UUID> {

    // Devuelve un representante por despacho (DISTINCT ON es PostgreSQL nativo)
    @Query(value = """
        SELECT DISTINCT ON (COALESCE(despacho_id, id))
               *
        FROM notificaciones
        ORDER BY COALESCE(despacho_id, id), fecha_envio DESC
        """, nativeQuery = true)
    List<Notificacion> findOnePerDispatch();

    void deleteByDespachoId(UUID despachoId);
}
