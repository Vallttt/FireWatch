package com.vallesol.bff.controller;

<<<<<<< HEAD:bff-service/src/main/java/com/vallesol/bff/controller/DashboardController.java
import com.vallesol.bff.client.AlertasClient;
import com.vallesol.bff.client.BrigadeClient;
=======
import com.vallesol.bff.client.GeoClient;
import com.vallesol.bff.client.NotificacionesClient;
>>>>>>> 2008e67 (separacion de SolAlertas en alert-service y notification-service con Eureka):BFF-ValleSol/src/main/java/com/vallesol/bff/controller/DashboardController.java
import com.vallesol.bff.client.ReportesClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

<<<<<<< HEAD:bff-service/src/main/java/com/vallesol/bff/controller/DashboardController.java
    private final AlertasClient alertasClient;
    private final ReportesClient reportesClient;
    private final BrigadeClient brigadeClient;
=======
    @Autowired
    private NotificacionesClient notificacionesClient;

    @Autowired
    private ReportesClient reportesClient;

    @Autowired
    private GeoClient geoClient;
>>>>>>> 2008e67 (separacion de SolAlertas en alert-service y notification-service con Eureka):BFF-ValleSol/src/main/java/com/vallesol/bff/controller/DashboardController.java

    @GetMapping("/stats")
    public Map<String, Object> gatherStatistics() {

        Map<String, Object> masterReport = new HashMap<>();

        try {
            int fires = reportesClient.getTotalActiveFires();
            // alertasEmitidas = total notifications dispatched (lives in notification-service)
            int alerts = notificacionesClient.getTotalCount();

            int brigades = 0;

            try {
                brigades = (int) brigadeClient.findAllBrigades()
                        .stream()
                        .filter(brigade -> "DEPLOYED".equals(brigade.getStatus()))
                        .count();

            } catch (Exception brigadeEx) {
                System.out.println("Brigade Service unavailable: " + brigadeEx.getMessage());
            }

            masterReport.put("totalIncendios", fires);
            masterReport.put("alertasEmitidas", alerts);
            masterReport.put("brigadasActivas", brigades);
            masterReport.put("estadoGlobal", fires > 3 ? "CRÍTICO" : "ESTABLE");
            masterReport.put("status", "BFF_SUCCESS");

        } catch (Exception e) {
            masterReport.put("totalIncendios", 0);
            masterReport.put("alertasEmitidas", 0);
            masterReport.put("brigadasActivas", 0);
            masterReport.put("estadoGlobal", "SIN CONEXIÓN");
            masterReport.put("status", "BFF_ERROR_SERVICES_DOWN");
        }

        return masterReport;
    }
}