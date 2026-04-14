package com.roomres.booking_service.publisher;

import com.roomres.booking_service.config.RabbitMQConfig;
import com.roomres.booking_service.dto.ReservaCriadaEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingMessagePublisher {

    private static final Logger log = LoggerFactory.getLogger(BookingMessagePublisher.class);
    private final RabbitTemplate rabbitTemplate;

    public void publishReservaCriada(ReservaCriadaEvent event) {
        log.info("Publicando evento no RabbitMQ para a reserva: {}", event.getBookingId());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_RESERVA,
                RabbitMQConfig.ROUTING_KEY_RESERVA_CRIADA,
                event
        );
    }
}