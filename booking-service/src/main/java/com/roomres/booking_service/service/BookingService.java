package com.roomres.booking_service.service;

import com.roomres.booking_service.model.Booking;
import com.roomres.booking_service.model.BookingStatus;
import com.roomres.booking_service.repository.BookingRepository;
import com.roomres.booking_service.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;

    public List<Booking> findAll() {
        return bookingRepository.findAll();
    }

    public Booking findById(UUID id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Reserva não encontrada com o ID: " + id));
    }

    @Transactional
    public Booking create(Booking booking) {
        // TODO: Validar se a sala existe chamando o room-service via Feign
        // TODO: Validar se o usuário existe chamando o user-service via Feign

        return bookingRepository.save(booking);
    }

    @Transactional
    public void cancel(UUID id) {
        Booking booking = findById(id);
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }
}