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

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<BookingNotificationDTO>> streamNotifications() {

        // CORREÇÃO: O primeiro ping vai em ZERO segundos, depois a cada 15s.
        // Isso impede que o Gateway ache que a conexão morreu por inatividade inicial.
        Flux<ServerSentEvent<BookingNotificationDTO>> ping = Flux.interval(Duration.ZERO, Duration.ofSeconds(15))
                .map(i -> ServerSentEvent.<BookingNotificationDTO>builder()
                        .event("ping")
                        .comment("keep-alive")
                        .build());

        Flux<ServerSentEvent<BookingNotificationDTO>> events = notificationSink.getFlux()
                .map(dto -> ServerSentEvent.<BookingNotificationDTO>builder()
                        .id(dto.getId().toString())
                        .event("booking-created")
                        .data(dto)
                        .build());

        return Flux.merge(ping, events);
    }
}