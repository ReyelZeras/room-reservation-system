package com.roomres.notification_service.controller;

import com.roomres.notification_service.component.NotificationSink;
import com.roomres.notification_service.dto.BookingNotificationDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Endpoints de tempo real para streaming de eventos")
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationSink notificationSink;

    @Value("${sse.limit:1}")
    private int sseLimit;

    @Operation(summary = "Streaming de Notificações", description = "Canal SSE para o frontend receber alertas.")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<BookingNotificationDTO>> streamNotifications() {

        Flux<ServerSentEvent<BookingNotificationDTO>> pingFlux = Flux.interval(Duration.ofSeconds(10))
                .map(seq -> ServerSentEvent.<BookingNotificationDTO>builder()
                        .event("ping")
                        .data(new BookingNotificationDTO())
                        .build());

        Flux<ServerSentEvent<BookingNotificationDTO>> eventFlux = notificationSink.getFlux()
                .map(dto -> ServerSentEvent.<BookingNotificationDTO>builder()
                        .event("notification")
                        .data(dto)
                        .build())
                .doOnNext(e -> log.info("🔥 [SSE] EVENTO REAL disparado para o túnel!"));

        return Flux.merge(pingFlux, eventFlux)
                .take(sseLimit)
                .doOnTerminate(() -> log.info("🔌 [SSE] Conexão encerrada intencionalmente para forçar o Flush da rede."));
    }
}