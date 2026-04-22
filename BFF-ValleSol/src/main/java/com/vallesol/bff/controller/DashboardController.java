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
    public Map<String, Object> recopilarEstadisticas() {
        Map<String, Object> reporteMaestro = new HashMap<>();

        try {
            int incendios = reportesClient.obtenerTotalFocosActivos();
            int alertas = alertasClient.obtenerTotalAlertas();

            // Contar brigadas reales desde Geo Service
            int brigadas = 0;
            try {
                Map<String, Object> geoResponse = geoClient.getBrigades();
                Object dataObj = geoResponse.get("data");
                if (dataObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> brigadeList = (List<Map<String, Object>>) dataObj;
                    brigadas = (int) brigadeList.stream()
                        .filter(b -> "DEPLOYED".equals(b.get("status")))
                        .count();
                }
            } catch (Exception geoEx) {
                System.out.println("Geo Service no disponible para brigadas: " + geoEx.getMessage());
            }

            reporteMaestro.put("totalIncendios", incendios);
            reporteMaestro.put("alertasEmitidas", alertas);
            reporteMaestro.put("brigadasActivas", brigadas);
            reporteMaestro.put("estadoGlobal", incendios > 3 ? "CRÍTICO" : "ESTABLE");
            reporteMaestro.put("status", "BFF_SUCCESS");

        } catch (Exception e) {
            reporteMaestro.put("totalIncendios", 0);
            reporteMaestro.put("alertasEmitidas", 0);
            reporteMaestro.put("brigadasActivas", 0);
            reporteMaestro.put("estadoGlobal", "SISTEMA DESCONECTADO");
            reporteMaestro.put("status", "BFF_ERROR_SERVICIOS_CAIDOS");
        }

        return reporteMaestro;
    }
}
