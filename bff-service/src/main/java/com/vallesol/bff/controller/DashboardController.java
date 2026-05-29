package com.vallesol.bff.controller;

import com.vallesol.bff.client.AlertasClient;
import com.vallesol.bff.client.BrigadeClient;
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

    private final AlertasClient alertasClient;
    private final ReportesClient reportesClient;
    private final BrigadeClient brigadeClient;

    @GetMapping("/stats")
    public Map<String, Object> gatherStatistics() {

        Map<String, Object> masterReport = new HashMap<>();

        try {
            int fires = reportesClient.getTotalActiveFires();
            int alerts = alertasClient.getTotalAlerts();

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