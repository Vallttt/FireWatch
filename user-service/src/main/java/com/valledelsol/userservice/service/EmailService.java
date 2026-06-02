package com.valledelsol.userservice.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

//    public void sendPasswordResetCode(String to, String code){
//        SimpleMailMessage message = new SimpleMailMessage();
//
//        message.setTo(to);
//
//        message.setSubject("Codigo de recuperacion - PrivData");
//
//        message.setText("Hola,\n\n" +
//                "Tu código de recuperación de contraseña es: " + code + "\n\n" +
//                "Este código expira en 10 minutos.\n\n" +
//                "Si no solicitaste este cambio, ignora este correo.\n\n" +
//                "Saludos,\n" +
//                "Equipo PrivData");
//
//        mailSender.send(message);
//    }

    public void sendPasswordResetCode(String to, String code) {

        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setFrom("w.vinet.h@gmail.com", "FireWatch - Municipalidad Valle del Sol");
            helper.setSubject("Código de recuperación - PrivData");

            String html = """
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 24px; background-color: #ffffff;">
                    
                        <!-- Encabezado -->
                        <div style="text-align: center; margin-bottom: 24px;">
                            <h1 style="color: #B91C1C; margin-bottom: 5px;">
                                🔥 FireWatch
                            </h1>
                            <p style="color: #6B7280; font-size: 14px; margin: 0;">
                                Municipalidad Valle del Sol
                            </p>
                        </div>
                    
                        <!-- Mensaje -->
                        <p style="font-size: 16px; color: #374151;">
                            Hemos recibido una solicitud para restablecer la contraseña de tu cuenta.
                        </p>
                    
                        <p style="font-size: 16px; color: #374151;">
                            Para continuar con el proceso, utiliza el siguiente código de verificación:
                        </p>
                    
                        <!-- Código -->
                        <div style="
                            background: linear-gradient(135deg, #DC2626, #EA580C);
                            color: white;
                            padding: 25px;
                            text-align: center;
                            border-radius: 10px;
                            margin: 30px 0;
                            box-shadow: 0 4px 10px rgba(0,0,0,0.15);
                        ">
                            <p style="margin: 0; font-size: 14px; text-transform: uppercase;">
                                Código de verificación
                            </p>
                    
                            <h1 style="
                                font-size: 42px;
                                letter-spacing: 8px;
                                margin: 12px 0;
                                font-weight: bold;
                            ">
                                %s
                            </h1>
                        </div>
                    
                        <!-- Información -->
                        <div style="
                            background-color: #FEF3C7;
                            border-left: 4px solid #F59E0B;
                            padding: 12px;
                            margin-bottom: 20px;
                            border-radius: 4px;
                        ">
                            <p style="margin: 0; color: #92400E; font-size: 14px;">
                                ⏳ Este código expirará en <strong>10 minutos</strong>.
                            </p>
                        </div>
                    
                        <p style="font-size: 14px; color: #6B7280;">
                            Si no solicitaste este cambio de contraseña, puedes ignorar este correo de forma segura.
                        </p>
                    
                        <hr style="
                            margin: 30px 0;
                            border: none;
                            border-top: 1px solid #E5E7EB;
                        " />
                    
                        <!-- Footer -->
                        <div style="text-align: center;">
                            <p style="font-size: 12px; color: #9CA3AF; margin: 0;">
                                FireWatch · Municipalidad Valle del Sol
                            </p>
                    
                            <p style="font-size: 12px; color: #9CA3AF; margin-top: 6px;">
                                Sistema de monitoreo y gestión de emergencias forestales.
                            </p>
                        </div>
                    
                    </div>
                """.formatted(code);

            helper.setText(html, true);

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Error al enviar correo de recuperación", e);
        }
    }
}
