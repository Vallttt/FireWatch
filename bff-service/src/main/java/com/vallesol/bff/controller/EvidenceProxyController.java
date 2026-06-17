package com.vallesol.bff.controller;

import com.vallesol.bff.client.EvidenceClient;
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
 * BFF Proxy → Evidence Service
 * Recibe peticiones del frontend (via Gateway) y las reenvia al microservicio de Evidencias.
 */
@RestController
@RequestMapping("/api/evidencias")
public class EvidenceProxyController {

    @Autowired
    private EvidenceClient evidenceClient;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ms.evidence.url:http://localhost:8088}")
    private String msEvidenceUrl;

    /**
     * Subir media (fotos/videos) para un reporte. Reenvia la peticion multipart al
     * Evidence Service usando RestTemplate (Feign no maneja bien el reenvio de MultipartFile).
     */
    @PostMapping(value = "/{reporteId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> subirMedia(
            @PathVariable String reporteId,
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

            String url = msEvidenceUrl + "/api/evidencias/" + reporteId;
            ResponseEntity<List> response = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(form, headers), List.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Could not upload media", "message", e.getMessage()));
        }
    }

    /**
     * Devolver todas las evidencias (como base64 data-URLs) de un reporte.
     */
    @GetMapping("/{reporteId}")
    public ResponseEntity<?> getEvidencias(@PathVariable String reporteId) {
        try {
            List<Map<String, Object>> response = evidenceClient.getEvidencias(reporteId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Evidence Service unavailable", "message", e.getMessage()));
        }
    }

    /** Contar evidencias de un reporte. */
    @GetMapping("/{reporteId}/count")
    public ResponseEntity<?> countEvidencias(@PathVariable String reporteId) {
        try {
            Integer total = evidenceClient.countEvidencias(reporteId);
            return ResponseEntity.ok(total != null ? total : 0);
        } catch (Exception e) {
            return ResponseEntity.ok(0);
        }
    }

    @DeleteMapping("/{reporteId}")
    public ResponseEntity<?> deleteEvidencias(@PathVariable String reporteId) {
        try {
            evidenceClient.deleteEvidencias(reporteId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Evidence Service unavailable", "message", e.getMessage()));
        }
    }
}
