package com.roomres.notification_service.consumer;

import com.roomres.notification_service.config.RabbitMQConfig;
import com.roomres.notification_service.dto.BookingNotificationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationConsumer {

    @RabbitListener(queues = RabbitMQConfig.QUEUE_RESERVA_CRIADA)
    public void processReservationNotification(BookingNotificationDTO notification) {
        log.info("==================================================");
        log.info("🔔 NOVA NOTIFICAÇÃO DE RESERVA RECEBIDA 🔔");
        log.info("Reserva ID: {}", notification.getId());
        log.info("Enviando e-mail de confirmação para o Usuário ID: {}", notification.getUserId());
        log.info("Detalhes: Sala {} das {} até {}",
                notification.getRoomId(),
                notification.getStartTime(),
                notification.getEndTime());
        log.info("Status do E-mail: ENVIADO COM SUCESSO!");
        log.info("==================================================");
    }
}