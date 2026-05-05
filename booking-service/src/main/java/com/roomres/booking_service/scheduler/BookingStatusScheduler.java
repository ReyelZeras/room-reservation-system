package com.roomres.booking_service.scheduler;

import com.roomres.booking_service.model.Booking;
import com.roomres.booking_service.model.BookingStatus;
import com.roomres.booking_service.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@Configuration
@EnableScheduling // Liga o "relogio" do Spring de forma isolada nesta classe
@RequiredArgsConstructor
public class BookingStatusScheduler {

    private final BookingRepository bookingRepository;

    // fixedRate = 60000 significa que vai rodar de 60 em 60 segundos
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void updateExpiredBookings() {
        LocalDateTime now = LocalDateTime.now();

        // Busca todas as reservas que estao CONFIRMADAS mas a hora de fim ja passou
        List<Booking> expiredBookings = bookingRepository.findByStatusAndEndTimeBefore(BookingStatus.CONFIRMED, now);

        if (!expiredBookings.isEmpty()) {
            log.info("Encontradas {} reservas expiradas. Atualizando status para COMPLETED...", expiredBookings.size());

            // Altera o status para COMPLETED
            expiredBookings.forEach(booking -> booking.setStatus(BookingStatus.COMPLETED));

            // Salva todas as alteracoes no banco
            bookingRepository.saveAll(expiredBookings);

            log.info("Reservas atualizadas com sucesso.");
        }
    }
}