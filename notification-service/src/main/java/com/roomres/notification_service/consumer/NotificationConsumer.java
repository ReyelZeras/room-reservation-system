package com.roomres.notification_service.consumer;

import com.roomres.notification_service.config.RabbitMQConfig;
import com.roomres.notification_service.component.NotificationSink;
import com.roomres.notification_service.dto.BookingNotificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    // Injeta o canal reativo
    private final NotificationSink notificationSink;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_RESERVA_CRIADA)
    public void processReservationNotification(BookingNotificationDTO notification) {
        log.info("==================================================");
        log.info("🔔 NOTIFICAÇÃO PROCESSADA (ID da Reserva: {})", notification.getId());
        log.info("==================================================");

        // Empurra a mensagem em tempo real para quem estiver conectado!
        notificationSink.publish(notification);
    }
}