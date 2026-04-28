package com.roomres.notification_service.controller;

import com.roomres.notification_service.component.NotificationSink;
import com.roomres.notification_service.dto.BookingNotificationDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Endpoints de tempo real para streaming de eventos")
@CrossOrigin(origins = "*") // Garante que o Angular consegue aceder
public class NotificationController {

    private final NotificationSink notificationSink;

    @Operation(summary = "Conectar ao Streaming (SSE)", description = "Abre um canal unidirecional (Server-Sent Events) para receber notificações de novas reservas em tempo real. Mantém a conexão viva enviando um 'ping' a cada 15 segundos.")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<BookingNotificationDTO>> streamNotifications() {

        Flux<ServerSentEvent<BookingNotificationDTO>> pingFlux = Flux.interval(Duration.ZERO, Duration.ofSeconds(15))
                .map(sequence -> ServerSentEvent.<BookingNotificationDTO>builder()
                        .event("ping")
                        .comment("keep-alive")
                        .build());

        Flux<ServerSentEvent<BookingNotificationDTO>> eventFlux = notificationSink.getFlux()
                .map(dto -> ServerSentEvent.<BookingNotificationDTO>builder()
                        .event("nova-reserva")
                        .data(dto)
                        .build());

        return Flux.merge(pingFlux, eventFlux);
    }


}