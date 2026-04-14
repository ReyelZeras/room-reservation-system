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
        // 1. Validação de Datas
        if (dto.getStartTime().isAfter(dto.getEndTime()) || dto.getStartTime().isEqual(dto.getEndTime())) {
            throw new BusinessException("Erro: O horário de início deve ser anterior ao horário de término.");
        }

        // 2. Validação de Conflitos
        if (bookingRepository.existsConflictingBooking(dto.getRoomId(), dto.getStartTime(), dto.getEndTime())) {
            throw new BusinessException("Conflito: Esta sala já possui uma reserva confirmada neste horário.");
        }

        // 3. Validação Externa: Room Service (via Feign)
        try {
            roomClient.getRoomById(dto.getRoomId());
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new BusinessException("Erro: A sala informada não existe no Room Service.");
            }
            throw new BusinessException("Erro: Room Service está offline ou indisponível.");
        }

        // 4. Validação Externa: User Service (via Feign)
        try {
            userClient.getUserById(dto.getUserId());
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new BusinessException("Erro: O usuário informado não existe no User Service.");
            }
            throw new BusinessException("Erro: User Service está offline ou indisponível.");
        }

        // 5. Persistência
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

        // 6. Mensageria Assíncrona (RabbitMQ)
        try {
            eventPublisher.sendReservationCreatedEvent(response);
        } catch (Exception e) {
            // Logamos o erro mas permitimos que a requisição finalize com sucesso,
            // pois o dado já está salvo no banco.
            log.error("Falha ao publicar evento no RabbitMQ para a reserva {}", savedBooking.getId(), e);
        }

        return response;
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

    private BookingResponseDTO mapToResponse(Booking b) {
        return new BookingResponseDTO(b);
    }
}