package com.ValleSol.notificationservice.repository;

import com.ValleSol.notificationservice.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, UUID> {

   
    @Query(value = """
            SELECT n.*
            FROM notificaciones n
            INNER JOIN (
                SELECT COALESCE(despacho_id, id) AS group_key, MAX(fecha_envio) AS max_fecha
                FROM notificaciones
                GROUP BY COALESCE(despacho_id, id)
            ) g ON COALESCE(n.despacho_id, n.id) = g.group_key
               AND n.fecha_envio = g.max_fecha
            ORDER BY n.fecha_envio DESC
            """, nativeQuery = true)
    List<Notificacion> findOnePerDispatch();


    List<Notificacion> findByTipoDestinatario(String tipoDestinatario);

    
    List<Notificacion> findByNivelEmergencia(String nivelEmergencia);

    void deleteByDespachoId(UUID despachoId);

    @Query(value = """
            SELECT COUNT(DISTINCT COALESCE(despacho_id, id))
            FROM notificaciones
            """, nativeQuery = true)
    Integer countUniqueDispatches();
}
