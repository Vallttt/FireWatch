package com.ValleSol.notificationservice.service;

import com.ValleSol.notificationservice.model.Notificacion;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Envía correos HTML reales via Brevo SMTP.
 *
 * Se invoca desde NotificacionStrategy para cada Notificacion cuyo
 * tipoAlerta NO sea "PUSH" (el push queda para una implementación futura).
 *
 * Las plantillas varían según el tipo de destinatario:
 *   EMAIL   → alerta general para ciudadanos
 *   BRIGADA → despacho operacional urgente
 *   ADMIN   → aviso administrativo ejecutivo
 */
@Service
@RequiredArgsConstructor
public class EmailSenderService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:w.vinet.h@gmail.com}")
    private String from;

    /**
     * Envía un correo HTML basado en la Notificacion persistida.
     * Los errores son no-fatales: se loguean pero no propagan la excepción
     * para que el resto de usuarios del despacho sigan recibiendo su correo.
     */
    public void enviarAlerta(Notificacion notif) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(notif.getDestinatario());
            helper.setFrom(from, "FireWatch — Municipalidad Valle del Sol");
            helper.setSubject(buildSubject(notif));
            helper.setText(buildHtml(notif), true);

            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("[notification-service] ⚠ Error al enviar correo a "
                    + notif.getDestinatario() + ": " + e.getMessage());
        }
    }

    // ─── subject ─────────────────────────────────────────────────────────────

    private String buildSubject(Notificacion notif) {
        String nivel = notif.getNivelEmergencia() != null ? notif.getNivelEmergencia() : "BAJO";
        return switch (notif.getTipoAlerta() != null ? notif.getTipoAlerta() : "EMAIL") {
            case "BRIGADA" -> "🚒 DESPACHO INMEDIATO [" + nivel + "] — Municipalidad Valle del Sol";
            case "ADMIN"   -> "📋 Aviso Administrativo [" + nivel + "] — FireWatch";
            default        -> "🔥 Alerta FireWatch [" + nivel + "] — Municipalidad Valle del Sol";
        };
    }

    // ─── HTML templates ───────────────────────────────────────────────────────

    private String buildHtml(Notificacion notif) {
        String tipo = notif.getTipoAlerta() != null ? notif.getTipoAlerta() : "EMAIL";
        return switch (tipo) {
            case "BRIGADA" -> brigadaHtml(notif);
            case "ADMIN"   -> adminHtml(notif);
            default        -> generalHtml(notif);
        };
    }

    /** Plantilla para ciudadanos — alerta general */
    private String generalHtml(Notificacion notif) {
        String nivel  = nivel(notif);
        String color  = nivelColor(notif);
        String badge  = nivelBadge(notif);
        String msg    = escape(notif.getMensaje());

        return """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:auto;padding:24px;background:#fff;">

                <div style="text-align:center;margin-bottom:24px;">
                    <h1 style="color:#B91C1C;margin-bottom:4px;">🔥 FireWatch</h1>
                    <p style="color:#6B7280;font-size:14px;margin:0;">Municipalidad Valle del Sol</p>
                </div>

                <div style="background:%s;color:#fff;padding:14px 20px;border-radius:8px;text-align:center;margin-bottom:24px;">
                    <span style="font-size:20px;font-weight:bold;">%s</span>
                    <p style="margin:4px 0 0;font-size:13px;opacity:.9;">Nivel de Emergencia: %s</p>
                </div>

                <p style="font-size:15px;color:#374151;">
                    Se ha generado una nueva alerta en el sistema de monitoreo FireWatch:
                </p>

                <div style="background:#F9FAFB;border-left:4px solid %s;padding:16px;border-radius:4px;margin:20px 0;">
                    <p style="margin:0;font-size:15px;color:#111827;white-space:pre-line;">%s</p>
                </div>

                <p style="font-size:13px;color:#6B7280;">
                    Este aviso es generado automáticamente por el sistema de gestión de emergencias.
                    Por favor, sigue las instrucciones de las autoridades locales.
                </p>

                <hr style="margin:24px 0;border:none;border-top:1px solid #E5E7EB;"/>
                <div style="text-align:center;">
                    <p style="font-size:12px;color:#9CA3AF;margin:0;">FireWatch · Municipalidad Valle del Sol</p>
                    <p style="font-size:12px;color:#9CA3AF;margin-top:4px;">Sistema de monitoreo y gestión de emergencias forestales.</p>
                </div>
            </div>
        """.formatted(color, badge, nivel, color, msg);
    }

    /** Plantilla para brigadas — despacho operacional */
    private String brigadaHtml(Notificacion notif) {
        String nivel = nivel(notif);
        String color = nivelColor(notif);
        String msg   = escape(notif.getMensaje());

        return """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:auto;padding:24px;background:#fff;">

                <div style="text-align:center;margin-bottom:20px;">
                    <h1 style="color:#B91C1C;margin-bottom:4px;">🔥 FireWatch</h1>
                    <p style="color:#6B7280;font-size:14px;margin:0;">Municipalidad Valle del Sol — Operaciones</p>
                </div>

                <div style="background:#7C0000;color:#fff;padding:16px 20px;border-radius:8px;text-align:center;margin-bottom:24px;">
                    <p style="margin:0;font-size:13px;text-transform:uppercase;letter-spacing:1px;">Despacho Inmediato</p>
                    <span style="font-size:22px;font-weight:bold;">🚒 BRIGADAS — ACCIÓN REQUERIDA</span>
                    <p style="margin:6px 0 0;font-size:13px;opacity:.9;">Nivel: %s</p>
                </div>

                <div style="background:#FEF2F2;border:2px solid #B91C1C;padding:16px;border-radius:6px;margin-bottom:20px;">
                    <p style="margin:0;font-size:15px;color:#111827;font-weight:bold;">Instrucciones de despacho:</p>
                    <p style="margin:10px 0 0;font-size:14px;color:#374151;white-space:pre-line;">%s</p>
                </div>

                <div style="background:#FFF7ED;border-left:4px solid #EA580C;padding:12px;border-radius:4px;margin-bottom:20px;">
                    <p style="margin:0;font-size:13px;color:#92400E;font-weight:bold;">
                        ⚡ Activar protocolo de emergencia. Reportar posición al centro de coordinación.
                    </p>
                </div>

                <hr style="margin:24px 0;border:none;border-top:1px solid #E5E7EB;"/>
                <div style="text-align:center;">
                    <p style="font-size:12px;color:#9CA3AF;margin:0;">FireWatch · Municipalidad Valle del Sol</p>
                </div>
            </div>
        """.formatted(nivel, msg);
    }

    /** Plantilla para administradores — aviso ejecutivo */
    private String adminHtml(Notificacion notif) {
        String nivel = nivel(notif);
        String color = nivelColor(notif);
        String badge = nivelBadge(notif);
        String msg   = escape(notif.getMensaje());

        return """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:auto;padding:24px;background:#fff;">

                <div style="text-align:center;margin-bottom:20px;">
                    <h1 style="color:#1E3A5F;margin-bottom:4px;">🔥 FireWatch</h1>
                    <p style="color:#6B7280;font-size:14px;margin:0;">Dirección Ejecutiva — Municipalidad Valle del Sol</p>
                </div>

                <div style="background:#1E3A5F;color:#fff;padding:14px 20px;border-radius:8px;text-align:center;margin-bottom:24px;">
                    <p style="margin:0;font-size:13px;text-transform:uppercase;letter-spacing:1px;">Aviso Administrativo</p>
                    <span style="font-size:20px;font-weight:bold;">📋 Requiere Atención</span>
                </div>

                <table style="width:100%;border-collapse:collapse;margin-bottom:20px;">
                    <tr>
                        <td style="padding:8px;background:#F3F4F6;font-size:13px;color:#6B7280;width:140px;">Nivel de emergencia</td>
                        <td style="padding:8px;background:#F9FAFB;font-size:13px;color:#111827;font-weight:bold;">
                            <span style="background:%s;color:#fff;padding:2px 10px;border-radius:12px;">%s — %s</span>
                        </td>
                    </tr>
                </table>

                <div style="background:#F9FAFB;border-left:4px solid #1E3A5F;padding:16px;border-radius:4px;margin-bottom:20px;">
                    <p style="margin:0;font-size:14px;color:#374151;white-space:pre-line;">%s</p>
                </div>

                <div style="background:#EFF6FF;border-left:4px solid #3B82F6;padding:12px;border-radius:4px;margin-bottom:20px;">
                    <p style="margin:0;font-size:13px;color:#1E40AF;">
                        📌 Acción requerida: Supervisión, coordinación de recursos y seguimiento operacional.
                    </p>
                </div>

                <hr style="margin:24px 0;border:none;border-top:1px solid #E5E7EB;"/>
                <div style="text-align:center;">
                    <p style="font-size:12px;color:#9CA3AF;margin:0;">FireWatch · Municipalidad Valle del Sol</p>
                </div>
            </div>
        """.formatted(color, badge, nivel, msg);
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private String nivel(Notificacion notif) {
        return notif.getNivelEmergencia() != null ? notif.getNivelEmergencia() : "BAJO";
    }

    private String nivelColor(Notificacion notif) {
        return switch (nivel(notif)) {
            case "CRITICO" -> "#7C0000";
            case "ALTO"    -> "#B91C1C";
            case "MEDIO"   -> "#D97706";
            default        -> "#1D4ED8";
        };
    }

    private String nivelBadge(Notificacion notif) {
        return switch (nivel(notif)) {
            case "CRITICO" -> "🚨 CRÍTICO";
            case "ALTO"    -> "⚠️ ALTO";
            case "MEDIO"   -> "🔶 MEDIO";
            default        -> "ℹ️ BAJO";
        };
    }

    private String escape(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;");
    }
}
