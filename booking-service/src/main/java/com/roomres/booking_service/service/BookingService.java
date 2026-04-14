package com.roomres.booking_service.service;

import com.roomres.booking_service.client.RoomClient;
import com.roomres.booking_service.client.UserClient;
import com.roomres.booking_service.dto.BookingRequestDTO;
import com.roomres.booking_service.dto.BookingResponseDTO;
import com.roomres.booking_service.exception.BusinessException;
import com.roomres.booking_service.exception.NotFoundException;
import com.roomres.booking_service.model.Booking;
import com.roomres.booking_service.model.BookingStatus;
import com.roomres.booking_service.publisher.BookingEventPublisher;
import com.roomres.booking_service.repository.BookingRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomClient roomClient;
    private final UserClient userClient;
    private final BookingEventPublisher eventPublisher;

    @Transactional
    public BookingResponseDTO createBooking(BookingRequestDTO dto) {
        // Validação de horários lógicos
        if (dto.getStartTime().isAfter(dto.getEndTime()) || dto.getStartTime().isEqual(dto.getEndTime())) {
            throw new BusinessException("Erro: O horário de início deve ser anterior ao horário de término.");
        }

        // Validação de regra de negócio: Sobreposição
        if (bookingRepository.existsConflictingBooking(dto.getRoomId(), dto.getStartTime(), dto.getEndTime())) {
            throw new BusinessException("Conflito: Esta sala já possui uma reserva confirmada neste horário.");
        }

        // Validação Externa: Room Service (via Feign)
        try {
            roomClient.getRoomById(dto.getRoomId());
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new BusinessException("Erro: A sala informada não existe no Room Service.");
            }
            throw new BusinessException("Erro: Room Service está offline ou indisponível.");
        }

        // Validação Externa: User Service (via Feign)
        try {
            userClient.getUserById(dto.getUserId());
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new BusinessException("Erro: O usuário informado não existe no User Service.");
            }
            throw new BusinessException("Erro: User Service está offline ou indisponível.");
        }

        // Persistência
        Booking booking = Booking.builder()
                .id(UUID.randomUUID())
                .roomId(dto.getRoomId())
                .userId(dto.getUserId())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .status(BookingStatus.CONFIRMED)
                .createdAt(LocalDateTime.now())
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        BookingResponseDTO response = mapToResponse(savedBooking);

        // Envio do Evento Assíncrono para o RabbitMQ
        try {
            eventPublisher.sendReservationCreatedEvent(response);
        } catch (Exception e) {
            log.error("Erro ao enviar evento para o RabbitMQ: {}", e.getMessage());
        }

        return response;
    }

    // ADICIONADO: Reagendamento de Horário
    @Transactional
    public BookingResponseDTO rescheduleBooking(UUID bookingId, LocalDateTime newStart, LocalDateTime newEnd) {
        if (newStart.isAfter(newEnd) || newStart.isEqual(newEnd)) {
            throw new BusinessException("Erro: O horário de início deve ser anterior ao horário de término.");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Reserva não encontrada."));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException("Não é possível reagendar uma reserva cancelada.");
        }

        // Verifica conflitos IGNORANDO a reserva atual
        if (bookingRepository.existsConflictingBookingExcludingId(booking.getRoomId(), newStart, newEnd, bookingId)) {
            throw new BusinessException("Conflito: O novo horário entra em choque com outra reserva na mesma sala.");
        }

        booking.setStartTime(newStart);
        booking.setEndTime(newEnd);
        return mapToResponse(bookingRepository.save(booking));
    }

    public List<BookingResponseDTO> getAll() {
        return bookingRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    public BookingResponseDTO getById(UUID id) {
        return mapToResponse(bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reserva não encontrada.")));
    }

    public List<BookingResponseDTO> getByUserId(UUID userId) {
        return bookingRepository.findByUserId(userId).stream().map(this::mapToResponse).toList();
    }

    public List<BookingResponseDTO> getByRoomId(UUID roomId) {
        return bookingRepository.findByRoomId(roomId).stream().map(this::mapToResponse).toList();
    }

    @Transactional
    public void cancelBooking(UUID id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reserva não encontrada para cancelamento."));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException("A reserva já se encontra cancelada.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    // ADICIONADO: Exclusão permanente para fechar o CRUD
    @Transactional
    public void deleteBookingPermanently(UUID id) {
        if (!bookingRepository.existsById(id)) {
            throw new NotFoundException("Reserva não encontrada para exclusão.");
        }
        bookingRepository.deleteById(id);
    }

    private BookingResponseDTO mapToResponse(Booking b) {
        return new BookingResponseDTO(b);
    }
}