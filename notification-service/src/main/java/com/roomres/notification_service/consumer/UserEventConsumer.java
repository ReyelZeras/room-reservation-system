package com.roomres.notification_service.consumer;

import com.roomres.notification_service.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventConsumer {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_USER_REGISTERED)
    public void processUserRegistration(Map<String, String> payload) {
        String email = payload.get("email");
        String name = payload.get("name");
        String token = payload.get("token");
        String verificationUrl = "http://localhost:4200/verify?token=" + token;

        log.info("🔔 Recebido evento de novo usuário. Enviando e-mail de ativação para: {}", email);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("RoomRes - Ative a sua conta");

            // Template HTML elegante
            String htmlContent = "<h2>Bem-vindo ao RoomRes, " + name + "!</h2>"
                    + "<p>Obrigado por criar uma conta. Para começar a reservar salas, confirme o seu e-mail clicando no botão abaixo:</p>"
                    + "<a href='" + verificationUrl + "' style='display: inline-block; padding: 10px 20px; font-size: 16px; color: #fff; background-color: #2563EB; text-decoration: none; border-radius: 5px; margin-top: 15px;'>Ativar Minha Conta</a>"
                    + "<br><br><p>Ou copie e cole este link no seu navegador:</p>"
                    + "<p><small>" + verificationUrl + "</small></p>";

            helper.setText(htmlContent, true); // true = HTML
            mailSender.send(message);

            log.info("✅ E-mail de ativação enviado com sucesso!");
        } catch (Exception e) {
            log.error("❌ Erro crítico ao enviar e-mail de ativação: {}", e.getMessage());
        }
    }


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
                    + "<a href='" + resetUrl + "' style='display: inline-block; padding: 10px 20px; font-size: 16px; color: #fff; background-color: #EF4444; text-decoration: none; border-radius: 5px; margin-top: 15px;'>Redefinir Minha Senha</a>"
                    + "<br><br><p>Ou copie e cole este link no navegador:</p>"
                    + "<p><small>" + resetUrl + "</small></p>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("✅ E-mail de redefinição enviado com sucesso!");
        } catch (Exception e) {
            log.error("❌ Erro ao enviar e-mail de redefinição: {}", e.getMessage());
        }
    }
}