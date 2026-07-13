package com.example.evidencias;

import com.example.evidencias.dto.response.ReporteMediaResponseDTO;
import com.example.evidencias.model.ReporteMedia;
import com.example.evidencias.repository.ReporteMediaRepository;
import com.example.evidencias.service.ReporteMediaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvidenciasApplicationTests {

    @Mock
    private ReporteMediaRepository mediaRepository;

    @InjectMocks
    private ReporteMediaService reporteMediaService;

    @Test
    void guardarMedia_deberiaGuardarArchivoYRetornarDataUrl() {
        // Arrange
        UUID reporteId = UUID.randomUUID();
        UUID mediaId = UUID.randomUUID();

        byte[] contenido = "contenido-de-prueba".getBytes();

        MockMultipartFile file = new MockMultipartFile(
                "files",
                "foto-incendio.jpg",
                "image/jpeg",
                contenido
        );

        when(mediaRepository.save(any(ReporteMedia.class)))
                .thenAnswer(invocation -> {
                    ReporteMedia media = invocation.getArgument(0);
                    media.setId(mediaId);
                    return media;
                });

        // Act
        List<ReporteMediaResponseDTO> result =
                reporteMediaService.guardarMedia(
                        reporteId,
                        new MultipartFile[]{file}
                );

        // Assert
        assertEquals(1, result.size());

        ReporteMediaResponseDTO dto = result.get(0);

        assertEquals(mediaId, dto.getId());
        assertEquals("foto-incendio.jpg", dto.getFilename());
        assertEquals("image/jpeg", dto.getContentType());
        assertTrue(dto.getData().startsWith("data:image/jpeg;base64,"));

        ArgumentCaptor<ReporteMedia> captor =
                ArgumentCaptor.forClass(ReporteMedia.class);

        verify(mediaRepository).save(captor.capture());

        ReporteMedia mediaGuardada = captor.getValue();

        assertEquals(reporteId, mediaGuardada.getReporteId());
        assertArrayEquals(contenido, mediaGuardada.getDatos());
        assertNotNull(mediaGuardada.getFechaSubida());
    }

    @Test
    void guardarMedia_deberiaIgnorarArchivosVacios() {
        // Arrange
        UUID reporteId = UUID.randomUUID();

        MockMultipartFile archivoVacio = new MockMultipartFile(
                "files",
                "vacio.jpg",
                "image/jpeg",
                new byte[0]
        );

        // Act
        List<ReporteMediaResponseDTO> result =
                reporteMediaService.guardarMedia(
                        reporteId,
                        new MultipartFile[]{archivoVacio}
                );

        // Assert
        assertTrue(result.isEmpty());

        verify(mediaRepository, never()).save(any());
    }
}
