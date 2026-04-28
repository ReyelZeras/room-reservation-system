package com.roomres.notification_service.consumer;

import com.roomres.notification_service.component.NotificationSink;
import com.roomres.notification_service.config.RabbitMQConfig;
import com.roomres.notification_service.dto.BookingNotificationDTO;
import com.roomres.notification_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final EmailService emailService;
    private final NotificationSink notificationSink; // O NOSSO MEGAFONE REATIVO
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // ==========================================
    // 1. FLUXO DE RESERVAS (E-mail + SSE Angular)
    // ==========================================
    @RabbitListener(queues = RabbitMQConfig.QUEUE_RESERVA_CRIADA)
    public void processBookingEvent(BookingNotificationDTO bookingEvent) {
        log.info("==================================================");
        log.info("🔔 NOTIFICAÇÃO PROCESSADA (Sala: {})", bookingEvent.getRoomName());
        log.info("==================================================");

        // 1. Envia o e-mail
        emailService.sendBookingConfirmation(bookingEvent);

        // 2. Dispara a Notificação em Tempo Real (SSE) para o Painel do Administrador
        notificationSink.emitNext(bookingEvent);
    }

    // ==========================================
    // 2. FLUXO DE REGISTO DE UTILIZADOR
    // ==========================================
    @RabbitListener(queues = RabbitMQConfig.QUEUE_USER_REGISTERED)
    public void processUserRegistered(Map<String, String> payload) {
        String email = payload.get("email");
        String name = payload.get("name");
        String token = payload.get("token");
        String verificationUrl = "http://localhost:4200/verify?token=" + token;

        log.info("🔔 Recebido evento de registro. Enviando e-mail de ativação para: {}", email);

        try {
            jakarta.mail.internet.MimeMessage message = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("RoomRes - Ative sua Conta");

            String htmlContent = "<h2>Olá, " + name + "</h2>"
                    + "<p>Bem-vindo ao RoomRes! Por favor, ative a sua conta clicando no link abaixo:</p>"
                    + "<a href='" + verificationUrl + "' style='display: inline-block; padding: 10px 20px; font-size: 16px; color: #fff; background-color: #3B82F6; text-decoration: none; border-radius: 5px; margin-top: 15px;'>Ativar Minha Conta</a>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("❌ Erro ao enviar e-mail de ativação: {}", e.getMessage());
        }
    }

    // ==========================================
    // 3. FLUXO DE RECUPERAÇÃO DE SENHA
    // ==========================================
    @RabbitListener(queues = RabbitMQConfig.QUEUE_PASSWORD_RESET)
    public void processPasswordReset(Map<String, String> payload) {
        String email = payload.get("email");
        String name = payload.get("name");
        String token = payload.get("token");
        String resetUrl = "http://localhost:4200/reset-password?token=" + token;

        log.info("🔔 Recebido pedido de reset de senha. Enviando e-mail para: {}", email);

        try {
            jakarta.mail.internet.MimeMessage message = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("RoomRes - Recuperação de Senha");

            String htmlContent = "<h2>Olá, " + name + "</h2>"
                    + "<p>Recebemos um pedido para redefinir a senha da sua conta.</p>"
                    + "<p>Se não foi você, pode ignorar este e-mail em segurança.</p>"
                    + "<a href='" + resetUrl + "' style='display: inline-block; padding: 10px 20px; font-size: 16px; color: #fff; background-color: #EF4444; text-decoration: none; border-radius: 5px; margin-top: 15px;'>Redefinir Minha Senha</a>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("❌ Erro ao enviar e-mail de redefinição: {}", e.getMessage());
        }
    }
}