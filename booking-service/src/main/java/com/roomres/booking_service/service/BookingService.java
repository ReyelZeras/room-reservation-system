// ... (mantenha os imports iguais aos de antes) ...
package com.roomres.booking_service.service;

import com.roomres.booking_service.client.RoomClient;
import com.roomres.booking_service.client.UserClient;
import com.roomres.booking_service.dto.BookingRequestDTO;
import com.roomres.booking_service.dto.BookingResponseDTO;
import com.roomres.booking_service.exception.BusinessException;
import com.roomres.booking_service.exception.NotFoundException;
import com.roomres.booking_service.model.Booking;
import com.roomres.booking_service.model.BookingStatus;
import com.roomres.booking_service.repository.BookingRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomClient roomClient;
    private final UserClient userClient;

    public BookingResponseDTO createBooking(BookingRequestDTO dto) {
        if (dto.getStartTime().isAfter(dto.getEndTime()) || dto.getStartTime().isEqual(dto.getEndTime())) {
            throw new BusinessException("Erro: O horário de início deve ser anterior ao horário de término.");
        }

        if (bookingRepository.existsConflictingBooking(dto.getRoomId(), dto.getStartTime(), dto.getEndTime())) {
            throw new BusinessException("Conflito: Esta sala já possui uma reserva confirmada neste horário.");
        }

        try {
            roomClient.getRoomById(dto.getRoomId());
        } catch (FeignException e) {
            // AGORA VAI FUNCIONAR POIS O ROOM SERVICE RETORNA 404 CORRETAMENTE
            if (e.status() == 404) {
                throw new BusinessException("Erro: A sala informada não existe no Room Service.");
            }
            throw new BusinessException("Erro: Room Service está offline ou indisponível.");
        }

        try {
            userClient.getUserById(dto.getUserId());
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new BusinessException("Erro: O usuário informado não existe no User Service.");
            }
            throw new BusinessException("Erro: User Service está offline ou indisponível.");
        }

        Booking booking = Booking.builder()
                .id(UUID.randomUUID())
                .roomId(dto.getRoomId())
                .userId(dto.getUserId())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .status(BookingStatus.CONFIRMED)
                .createdAt(LocalDateTime.now())
                .build();

        return mapToResponse(bookingRepository.save(booking));
    }

    public List<BookingResponseDTO> getAll() {
        return bookingRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    public BookingResponseDTO getById(UUID id) {
        return mapToResponse(bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reserva não encontrada.")));
    }

    // NOVOS MÉTODOS
    public List<BookingResponseDTO> getByUserId(UUID userId) {
        return bookingRepository.findByUserId(userId).stream().map(this::mapToResponse).toList();
    }

    public List<BookingResponseDTO> getByRoomId(UUID roomId) {
        return bookingRepository.findByRoomId(roomId).stream().map(this::mapToResponse).toList();
    }

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