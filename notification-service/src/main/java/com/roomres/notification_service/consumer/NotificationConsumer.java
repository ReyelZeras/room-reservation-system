package com.roomres.notification_service.consumer;

import com.roomres.notification_service.component.NotificationSink;
import com.roomres.notification_service.config.RabbitMQConfig;
import com.roomres.notification_service.dto.BookingNotificationDTO;
import com.roomres.notification_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationSink notificationSink;
    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_RESERVA_CRIADA)
    public void processReservationNotification(BookingNotificationDTO notification) {
        log.info("==================================================");
        log.info("🔔 NOTIFICAÇÃO PROCESSADA (Sala: {})", notification.getRoomName());
        log.info("==================================================");

        notificationSink.publish(notification);

        if (notification.getUserEmail() != null) {
            // Passamos o objeto inteiro para o EmailService formatar
            emailService.sendBookingConfirmation(notification);
        } else {
            log.warn("Mensagem do RabbitMQ chegou sem o E-mail. E-mail não enviado.");
        }
    }
}