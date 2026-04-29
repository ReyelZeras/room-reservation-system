package com.roomres.notification_service.component;

import com.roomres.notification_service.dto.BookingNotificationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Component
public class NotificationSink {

    private static final Logger log = LoggerFactory.getLogger(NotificationSink.class);

    // A MÁGICA DA RESILIÊNCIA: directBestEffort()
    // Impede que o canal "morra" se o Front-end der F5 ou se todos os Admins deslogarem.
    private final Sinks.Many<BookingNotificationDTO> sink = Sinks.many().multicast().directBestEffort();

    public void emitNext(BookingNotificationDTO dto) {
        Sinks.EmitResult result = sink.tryEmitNext(dto);
        if (result.isFailure()) {
            log.warn("Aviso SSE: Mensagem não entregue à UI. Motivo: {}", result);
        }
    }

    public Flux<BookingNotificationDTO> getFlux() {
        return sink.asFlux();
    }
}