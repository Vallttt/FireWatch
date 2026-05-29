package com.vallesol.bff.controller;

import com.vallesol.bff.client.ReportesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * BFF Proxy → Report Service
 * Receives requests from the frontend (via Gateway) and forwards them to the Reports microservice.
 */
@RestController
@RequestMapping("/api/reportes")
public class ReportesProxyController {

    @Autowired
    private ReportesClient reportesClient;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ms.reportes.url:http://localhost:8081}")
    private String msReportesUrl;

    // ------------------------------------------------------------------ //
    //  Reports
    // ------------------------------------------------------------------ //

    @PostMapping
    public ResponseEntity<?> createReport(@RequestBody Map<String, Object> body) {
        try {
            Map<String, Object> response = reportesClient.createReport(body);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Report Service unavailable", "message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> listReports() {
        try {
            List<Map<String, Object>> response = reportesClient.listReports();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Report Service unavailable", "message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReport(@PathVariable String id) {
        try {
            Map<String, Object> response = reportesClient.getReport(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Report Service unavailable", "message", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> updateStatus(@PathVariable String id, @RequestBody Map<String, Object> body) {
        try {
            Map<String, Object> response = reportesClient.updateStatus(id, body);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Report Service unavailable", "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReport(@PathVariable String id) {
        try {
            reportesClient.deleteReport(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Report Service unavailable", "message", e.getMessage()));
        }
    }

    @GetMapping("/focos-activos")
    public ResponseEntity<?> getActiveFires() {
        try {
            Integer total = reportesClient.getTotalActiveFires();
            return ResponseEntity.ok(total != null ? total : 0);
        } catch (Exception e) {
            return ResponseEntity.ok(0);
        }
    }

    // ------------------------------------------------------------------ //
    //  Media (photos / videos attached to a report)
    // ------------------------------------------------------------------ //

    /**
     * Upload files for a report. Forwards the multipart request to the Report Service
     * using RestTemplate (Feign doesn't handle MultipartFile forwarding easily).
     */
    @PostMapping(value = "/{id}/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadMedia(
            @PathVariable String id,
            @RequestParam("files") MultipartFile[] files) {
        try {
            MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
            for (MultipartFile file : files) {
                final byte[] bytes = file.getBytes();
                final String filename = file.getOriginalFilename() != null
                        ? file.getOriginalFilename() : "file";
                final String contentType = file.getContentType() != null
                        ? file.getContentType() : "application/octet-stream";

                ByteArrayResource resource = new ByteArrayResource(bytes) {
                    @Override
                    public String getFilename() { return filename; }
                };

                HttpHeaders partHeaders = new HttpHeaders();
                partHeaders.setContentType(MediaType.parseMediaType(contentType));
                form.add("files", new HttpEntity<>(resource, partHeaders));
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            String url = msReportesUrl + "/api/reportes/" + id + "/media";
            ResponseEntity<List> response = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(form, headers), List.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Could not upload media", "message", e.getMessage()));
        }
    }

    /**
     * Retrieve all media (as base64 data-URLs) for a report.
     */
    @GetMapping("/{id}/media")
    public ResponseEntity<?> getMedia(@PathVariable String id) {
        try {
            List<Map<String, Object>> response = reportesClient.getMedia(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Could not load media", "message", e.getMessage()));
        }
    }
}
