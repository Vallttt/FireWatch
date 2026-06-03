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
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(notif.getDestinatario());
            helper.setFrom(from, "FireWatch — Municipalidad Valle del Sol");
            helper.setSubject(buildSubject(notif));
            helper.setText(notif.getMensaje() != null ? notif.getMensaje() : "", false);

            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("[notification-service] Error al enviar correo a "
                    + notif.getDestinatario() + ": " + e.getMessage());
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
}
