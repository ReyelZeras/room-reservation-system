package com.roomres.notification_service;

import com.roomres.notification_service.dto.BookingNotificationDTO;
import com.roomres.notification_service.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    private BookingNotificationDTO dto;

    @BeforeEach
    void setUp() {
        // Injeta manualmente o valor que o Spring pegaria do application.yml
        ReflectionTestUtils.setField(emailService, "fromEmail", "sistema@roomres.com");

        dto = new BookingNotificationDTO();
        dto.setId(UUID.randomUUID());
        dto.setRoomName("Sala Alpha");
        dto.setUserName("Administrador");
        dto.setUserEmail("admin@roomres.com");
        dto.setTitle("Reunião Importante");
        dto.setStatus("CONFIRMED");
        dto.setStartTime(LocalDateTime.now().plusDays(1).withNano(0));
        dto.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2).withNano(0));
    }

    @Test
    @DisplayName("Deve formatar e enviar o e-mail de confirmacao de reserva")
    void sendBookingConfirmation_Success() {
        // Act
        emailService.sendBookingConfirmation(dto);

        // Assert
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}