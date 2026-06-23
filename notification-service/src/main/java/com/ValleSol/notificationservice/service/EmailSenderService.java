package com.ValleSol.notificationservice.service;

import com.ValleSol.notificationservice.model.Notificacion;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailSenderService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:w.vinet.h@gmail.com}")
    private String from;

    public void enviarAlerta(Notificacion notif) {
        try {
            System.out.println(">>> Enviando alerta por EMAIL a: " + notif.getDestinatario());
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(notif.getDestinatario());
            helper.setFrom(from, "FireWatch — Municipalidad Valle del Sol");
            helper.setSubject(buildSubject(notif));
            helper.setText(buildHtml(notif), true);

            mailSender.send(message);
            System.out.println(">>> Correo enviado exitosamente a: " + notif.getDestinatario());

        } catch (Exception e) {
            System.out.println(">>> ERROR AL ENVIAR CORREO a " + notif.getDestinatario() + ": " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace(System.out);
        }
    }

    private String buildSubject(Notificacion notif) {
        String nivel = notif.getNivelEmergencia() != null ? notif.getNivelEmergencia() : "BAJO";
        if (notif.getTipoAlerta() == null) return "Alerta FireWatch [" + nivel + "]";
        return switch (notif.getTipoAlerta()) {
            case "BRIGADA" -> "DESPACHO INMEDIATO [" + nivel + "] — Municipalidad Valle del Sol";
            case "ADMIN"   -> "Aviso Administrativo [" + nivel + "] — FireWatch";
            default        -> "Alerta FireWatch [" + nivel + "] — Municipalidad Valle del Sol";
        };
    }

    private String buildHtml(Notificacion notif) {

        String nivel = notif.getNivelEmergencia() != null
                ? notif.getNivelEmergencia()
                : "BAJO";

        String tipoAlerta = notif.getTipoAlerta() != null ? notif.getTipoAlerta() : "MANUAL";

        // Si es una alerta de PREVENCIÓN (sin datos de reporte), usar template simplificado
        boolean esPrevencion = "Preventiva".equalsIgnoreCase(tipoAlerta) ||
                               (notif.getReporteId() == null && notif.getDescripcionReporte() == null);

        String descripcion = notif.getDescripcionReporte() != null
                ? notif.getDescripcionReporte()
                : "Sin descripción";

        String reportante = notif.getUsuarioReportante() != null
                ? notif.getUsuarioReportante()
                : "No informado";

        String fecha = "No disponible";

        if (notif.getFechaReporte() != null) {
            fecha = notif.getFechaReporte()
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }

        String zona = notif.getZoneName() != null
                ? notif.getZoneName()
                : "No informada";

        String lat = notif.getLatitude() != null
                ? String.valueOf(notif.getLatitude())
                : "-";

        String lon = notif.getLongitude() != null
                ? String.valueOf(notif.getLongitude())
                : "-";

        String reporteId = notif.getReporteId() != null
                ? notif.getReporteId().toString()
                : "-";

        String colorNivel = "#28a745";

        switch (nivel.toUpperCase()) {
            case "ALTO", "HIGH" -> colorNivel = "#dc3545";
            case "MEDIO", "MEDIUM" -> colorNivel = "#ffc107";
            case "BAJO", "LOW" -> colorNivel = "#28a745";
            case "CRÍTICO", "CRITICAL" -> colorNivel = "#8b0000";
        }

        // Si es alerta de PREVENCIÓN, usar template sin datos de reporte
        if (esPrevencion) {
            return buildPreventionHtml(notif, colorNivel, nivel);
        }

        return """
        <html>
        <body style="font-family: Arial, sans-serif; background-color:#f4f6f8; padding:20px;">
        
            <div style="max-width:700px; margin:auto; background:white; border-radius:10px; overflow:hidden; box-shadow:0 2px 8px rgba(0,0,0,0.1);">
                
                <div style="background:#1f4e79; color:white; padding:20px;">
                    <h2 style="margin:0;">🚨 FireWatch - Municipalidad Valle del Sol</h2>
                </div>

                <div style="padding:25px;">

                    <h3 style="color:#d9534f;">
                        Nuevo Reporte Registrado
                    </h3>

                    <p>
                        Se ha generado una nueva alerta dentro del sistema FireWatch.
                    </p>

                    <table style="width:100%%; border-collapse:collapse;">
                        <tr>
                            <td><strong>ID Reporte</strong></td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <td><strong>Nivel Emergencia</strong></td>
                            <td>
                                <span style="
                                    background:%s;
                                    color:white;
                                    padding:4px 10px;
                                    border-radius:5px;
                                    font-weight:bold;">
                                    %s
                                </span>
                            </td>
                        </tr>
                        <tr>
                            <td><strong>Reportante</strong></td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <td><strong>Fecha</strong></td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <td><strong>Zona</strong></td>
                            <td>%s</td>
                        </tr>
                    </table>

                    <hr/>

                    <h4>Descripción</h4>

                    <p>%s</p>

                    <h4>Ubicación</h4>

                    <ul>
                        <li><strong>Latitud:</strong> %s</li>
                        <li><strong>Longitud:</strong> %s</li>
                    </ul>

                    <hr/>

                    <p style="font-size:12px; color:#666;">
                        Este correo fue generado automáticamente por el Sistema FireWatch.
                    </p>

                </div>
            </div>

        </body>
        </html>
        """
                .formatted(
                        reporteId,
                        colorNivel,
                        nivel,
                        reportante,
                        fecha,
                        zona,
                        descripcion,
                        lat,
                        lon
                );
    }

    private String buildPreventionHtml(Notificacion notif, String colorNivel, String nivel) {
        String mensaje = notif.getMensaje() != null
                ? notif.getMensaje()
                : "Alerta comunitaria del sistema FireWatch";

        return """
        <html>
        <body style="font-family: Arial, sans-serif; background-color:#f4f6f8; padding:20px;">

            <div style="max-width:700px; margin:auto; background:white; border-radius:10px; overflow:hidden; box-shadow:0 2px 8px rgba(0,0,0,0.1);">

                <div style="background:%s; color:white; padding:20px;">
                    <h2 style="margin:0;">📢 FireWatch - Alerta Comunitaria</h2>
                </div>

                <div style="padding:25px;">

                    <h3 style="color:#d9534f;">
                        Alerta de Prevención y Seguridad
                    </h3>

                    <div style="background:#f9f3e6; border-left:4px solid %s; padding:15px; margin:15px 0;">
                        <p style="font-size:16px; margin:0; color:#333;">
                            %s
                        </p>
                    </div>

                    <p style="color:#555; line-height:1.6;">
                        Recibiste esta alerta porque estás registrado en el sistema de alertas comunitarias
                        de la Municipalidad Valle del Sol.
                    </p>

                    <hr/>

                    <h4>Recomendaciones:</h4>
                    <ul style="color:#555;">
                        <li>Mantente atento a nuevas actualizaciones</li>
                        <li>Comparte esta información con tu comunidad</li>
                        <li>Reporta cualquier situación de riesgo usando la aplicación FireWatch</li>
                    </ul>

                    <hr/>

                    <p style="font-size:12px; color:#666;">
                        Este correo fue generado automáticamente por el Sistema FireWatch.
                    </p>

                </div>
            </div>

        </body>
        </html>
        """
                .formatted(colorNivel, colorNivel, mensaje);
    }
}
