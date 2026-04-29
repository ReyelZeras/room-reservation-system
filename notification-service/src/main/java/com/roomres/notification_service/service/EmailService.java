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
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
            String startFormatted = dto.getStartTime().format(formatter);
            String endFormatted = dto.getEndTime().format(formatter);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(dto.getUserEmail());

            // MÁGICA AQUI: Verifica o status para decidir o Assunto e o Texto
            boolean isCancelled = "CANCELLED".equalsIgnoreCase(dto.getStatus());

            String subject = isCancelled ? "RoomRes - Reserva Cancelada" : "RoomRes - Confirmação de Reserva";

            String introText = isCancelled
                    ? "A sua reserva foi CANCELADA com sucesso no nosso sistema.\n\n"
                    : "A sua reserva foi processada e CONFIRMADA com sucesso no nosso sistema.\n\n";

            message.setSubject(subject);
            message.setText("Olá, " + dto.getUserName() + "!\n\n" +
                    introText +
                    "📌 Detalhes da Reserva:\n" +
                    "Titulo: " + dto.getTitle() + "\n" +
                    "Sala: " + dto.getRoomName() + "\n" +
                    "Entrada: " + startFormatted + "\n" +
                    "Saída: " + endFormatted + "\n" +
                    "Status: " + dto.getStatus() + "\n\n" +
                    "ID da Reserva: " + dto.getId() + "\n\n" +
                    "Obrigado por utilizar o sistema de reservas corporativo.");

            mailSender.send(message);
            log.info("📧 E-mail ({}) enviado com sucesso para: {}", dto.getStatus(), dto.getUserEmail());

        } catch (Exception e) {
            log.error("❌ Falha ao enviar e-mail para {}: {}", dto.getUserEmail(), e.getMessage());
        }
    }
}