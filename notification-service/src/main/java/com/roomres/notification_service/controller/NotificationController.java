package com.roomres.notification_service.controller;

import com.roomres.notification_service.component.NotificationSink;
import com.roomres.notification_service.dto.BookingNotificationDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Operation(summary = "Streaming de Notificações", description = "Canal SSE para o frontend receber alertas.")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<BookingNotificationDTO>> streamNotifications() {

        // 1. O Ping serve para manter a rede girando se ninguém fizer reservas
        Flux<ServerSentEvent<BookingNotificationDTO>> pingFlux = Flux.interval(Duration.ofSeconds(10))
                .map(seq -> ServerSentEvent.<BookingNotificationDTO>builder()
                        .event("ping")
                        .data(new BookingNotificationDTO())
                        .build());

        // 2. O Evento Real
        Flux<ServerSentEvent<BookingNotificationDTO>> eventFlux = notificationSink.getFlux()
                .map(dto -> ServerSentEvent.<BookingNotificationDTO>builder()
                        .event("notification")
                        .data(dto)
                        .build())
                .doOnNext(e -> log.info("🔥 [SSE] EVENTO REAL disparado para o túnel!"));

        // 3. A CARTADA FINAL: .take(1)
        // Isso faz o Java enviar EXATAMENTE 1 pacote (ou um ping ou a sua reserva) e logo depois FECHAR a conexão TCP.
        // O Fechamento obriga a Cloudflare a cuspir o pacote para a UI instantaneamente!
        return Flux.merge(pingFlux, eventFlux)
                .take(1)
                .doOnTerminate(() -> log.info("🔌 [SSE] Conexão encerrada intencionalmente para forçar o Flush da Cloudflare."));
    }
}