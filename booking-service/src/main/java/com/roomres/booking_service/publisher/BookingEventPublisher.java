package com.roomres.booking_service.publisher;

import com.roomres.booking_service.config.RabbitMQConfig;
import com.roomres.booking_service.dto.BookingResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void sendReservationCreatedEvent(BookingResponseDTO booking, String userEmail, String userName, String roomName) {
        log.info("Enviando evento de reserva enriquecido para o RabbitMQ...");

        Map<String, Object> payload = new HashMap<>();
        payload.put("id", booking.getId());
        payload.put("roomId", booking.getRoomId());
        payload.put("userId", booking.getUserId());
        payload.put("userEmail", userEmail);
        payload.put("userName", userName); // Novo
        payload.put("roomName", roomName); // Novo
        payload.put("startTime", booking.getStartTime());
        payload.put("endTime", booking.getEndTime());
        payload.put("status", booking.getStatus());
        payload.put("createdAt", booking.getCreatedAt());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_RESERVA,
                RabbitMQConfig.ROUTING_KEY_RESERVA_CRIADA,
                payload
        );
    }
}