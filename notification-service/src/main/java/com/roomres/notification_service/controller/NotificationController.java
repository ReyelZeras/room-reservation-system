package com.roomres.notification_service.controller;

import com.roomres.notification_service.component.NotificationSink;
import com.roomres.notification_service.dto.BookingNotificationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationSink notificationSink;

    // TEXT_EVENT_STREAM_VALUE é o que avisa ao navegador: "Não feche a conexão, é um stream!"
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<BookingNotificationDTO>> streamNotifications() {

        // HEARTBEAT (Ping): Manda um evento vazio a cada 15s apenas para a conexão não cair por inatividade
        Flux<ServerSentEvent<BookingNotificationDTO>> ping = Flux.interval(Duration.ofSeconds(15))
                .map(i -> ServerSentEvent.<BookingNotificationDTO>builder()
                        .event("ping")
                        .comment("keep-alive")
                        .build());

        // Eventos reais vindos do RabbitMQ
        Flux<ServerSentEvent<BookingNotificationDTO>> events = notificationSink.getFlux()
                .map(dto -> ServerSentEvent.<BookingNotificationDTO>builder()
                        .id(dto.getId().toString())
                        .event("booking-created") // Nome do evento que o JS vai escutar
                        .data(dto)
                        .build());

        // Junta o ping e os eventos no mesmo fluxo
        return Flux.merge(ping, events);
    }
}