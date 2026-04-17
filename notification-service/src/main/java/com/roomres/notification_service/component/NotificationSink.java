package com.roomres.notification_service.component;

import com.roomres.notification_service.dto.BookingNotificationDTO;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Component
public class NotificationSink {

    // Sinks.Many é um canal reativo (Publisher).
    // multicast() permite que VÁRIOS clientes se conectem ao mesmo tempo (ex: vários navegadores abertos)
    // onBackpressureBuffer() guarda a mensagem se o cliente demorar a ler
    private final Sinks.Many<BookingNotificationDTO> sink = Sinks.many().multicast().onBackpressureBuffer();

    public void publish(BookingNotificationDTO dto) {
        sink.tryEmitNext(dto); // Joga a notificação "no ar"
    }

    public Flux<BookingNotificationDTO> getFlux() {
        return sink.asFlux(); // Retorna o "cano" para os clientes se conectarem
    }
}