package com.roomres.notification_service.service;

import com.roomres.notification_service.dto.BookingNotificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendBookingConfirmation(BookingNotificationDTO dto) {
        try {
            // Formatador de Datas bonito para o Brasil
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
            String startFormatted = dto.getStartTime().format(formatter);
            String endFormatted = dto.getEndTime().format(formatter);

            // Se o nome vier nulo, chama de "Usuário"
            String clientName = dto.getUserName() != null ? dto.getUserName() : "Usuário";

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(dto.getUserEmail());
            message.setSubject("Reserva Confirmada - " + dto.getRoomName());

            // Corpo do E-mail Profissional
            message.setText("Olá, " + clientName + "!\n\n" +
                    "A sua reserva foi processada e confirmada com sucesso no nosso sistema.\n\n" +
                    "📌 Detalhes da Reserva:\n" +
                    "Sala: " + dto.getRoomName() + "\n" +
                    "Entrada: " + startFormatted + "\n" +
                    "Saída: " + endFormatted + "\n" +
                    "Status: " + dto.getStatus() + "\n\n" +
                    "ID da Reserva: " + dto.getId() + "\n\n" +
                    "Obrigado por utilizar o sistema de reservas corporativo.");

            mailSender.send(message);
            log.info("📧 E-mail formatado enviado com sucesso para: {}", dto.getUserEmail());
        } catch (Exception e) {
            log.error("❌ Falha ao enviar e-mail para {}: {}", dto.getUserEmail(), e.getMessage());
        }
    }
}