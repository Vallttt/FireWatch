package com.vallesol.bff.controller;

import com.vallesol.bff.client.AlertasClient;
import com.vallesol.bff.client.GeoClient;
import com.vallesol.bff.client.ReportesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private AlertasClient alertasClient;

    @Autowired
    private ReportesClient reportesClient;

    @Autowired
    private GeoClient geoClient;

    @GetMapping("/stats")
    public Map<String, Object> gatherStatistics() {
        Map<String, Object> masterReport = new HashMap<>();

        try {
            int fires = reportesClient.getTotalActiveFires();
            int alerts = alertasClient.getTotalAlerts();

            // Count real brigades from the Geo Service
            int brigades = 0;
            try {
                Map<String, Object> geoResponse = geoClient.getBrigades();
                Object dataObj = geoResponse.get("data");
                if (dataObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> brigadeList = (List<Map<String, Object>>) dataObj;
                    brigades = (int) brigadeList.stream()
                        .filter(b -> "DEPLOYED".equals(b.get("status")))
                        .count();
                }
            } catch (Exception geoEx) {
                System.out.println("Geo Service unavailable for brigades: " + geoEx.getMessage());
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
