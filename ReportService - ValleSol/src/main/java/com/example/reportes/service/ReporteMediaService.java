package com.example.reportes.service;

import com.example.reportes.dto.response.ReporteMediaResponseDTO;
import com.example.reportes.model.ReporteMedia;
import com.example.reportes.repository.ReporteMediaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class ReporteMediaService {

    @Autowired
    private ReporteMediaRepository mediaRepository;

    // ------------------------------------------------------------------ //
    //  guardar
    // ------------------------------------------------------------------ //

    public List<ReporteMediaResponseDTO> guardarMedia(UUID reporteId, MultipartFile[] files) {
        List<ReporteMediaResponseDTO> result = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            ReporteMedia media = new ReporteMedia();
            media.setReporteId(reporteId);
            media.setFilename(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "archivo"
            );
            media.setContentType(
                file.getContentType() != null ? file.getContentType() : "application/octet-stream"
            );
            media.setFechaSubida(LocalDateTime.now());

            try {
                media.setDatos(file.getBytes());
            } catch (IOException e) {
                System.err.println("Error reading file bytes: " + e.getMessage());
                continue;
            }

            ReporteMedia saved = mediaRepository.save(media);
            result.add(toDTO(saved));
        }

        return result;
    }

    // ------------------------------------------------------------------ //
    //  leer
    // ------------------------------------------------------------------ //

    public List<ReporteMediaResponseDTO> obtenerMedia(UUID reporteId) {
        List<ReporteMedia> lista = mediaRepository.findByReporteId(reporteId);
        List<ReporteMediaResponseDTO> result = new ArrayList<>();
        for (ReporteMedia m : lista) {
            result.add(toDTO(m));
        }
        return result;
    }

    public int contarMedia(UUID reporteId) {
        return mediaRepository.countByReporteId(reporteId);
    }

    // ------------------------------------------------------------------ //
    //  borrar
    // ------------------------------------------------------------------ //

    @Transactional
    public void eliminarMediaDeReporte(UUID reporteId) {
        mediaRepository.deleteByReporteId(reporteId);
    }

    // ------------------------------------------------------------------ //
    //  Mapear ReporteMedia -> ReporteMediaResponseDTO
    // ------------------------------------------------------------------ //

    private ReporteMediaResponseDTO toDTO(ReporteMedia media) {
        ReporteMediaResponseDTO dto = new ReporteMediaResponseDTO();
        dto.setId(media.getId());
        dto.setFilename(media.getFilename());
        dto.setContentType(media.getContentType());
    
        // codificar como una data-URL para que el navegador pueda mostrarlo directamente
        String base64 = Base64.getEncoder().encodeToString(media.getDatos());
        dto.setData("data:" + media.getContentType() + ";base64," + base64);
        dto.setFechaSubida(media.getFechaSubida());
        return dto;
    }
}
