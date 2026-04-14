package com.roomres.booking_service.publisher;

import com.roomres.booking_service.config.RabbitMQConfig;
import com.roomres.booking_service.dto.BookingResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void sendReservationCreatedEvent(BookingResponseDTO booking) {
        log.info("Enviando evento de reserva criada para o ID: {}", booking.getId());

        // Usando as constantes corrigidas do RabbitMQConfig
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_RESERVA,
                RabbitMQConfig.ROUTING_KEY_RESERVA_CRIADA,
                booking
        );
    }
}