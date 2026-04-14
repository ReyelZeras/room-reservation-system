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
import com.roomres.booking_service.publisher.AuditPublisher;
import com.roomres.booking_service.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomClient roomClient;
    private final UserClient userClient;
    private final BookingEventPublisher eventPublisher;
    private final AuditPublisher auditPublisher;

    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getAll() {
        return bookingRepository.findAll().stream()
                .map(BookingResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BookingResponseDTO getById(UUID id) {
        return bookingRepository.findById(id)
                .map(BookingResponseDTO::new)
                .orElseThrow(() -> new NotFoundException("Reserva não encontrada."));
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getByUserId(UUID userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(BookingResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getByRoomId(UUID roomId) {
        return bookingRepository.findByRoomId(roomId).stream()
                .map(BookingResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public BookingResponseDTO createBooking(BookingRequestDTO dto) {
        log.info("Iniciando criação de reserva...");
        validateDates(dto.getStartTime(), dto.getEndTime());

        // Usa o método do repositório para verificar conflitos
        if (bookingRepository.existsConflictingBooking(dto.getRoomId(), dto.getStartTime(), dto.getEndTime())) {
            throw new BusinessException("A sala já está reservada para o período selecionado.");
        }

        validateRoomAndUser(dto.getRoomId(), dto.getUserId());

        Booking booking = Booking.builder()
                .id(UUID.randomUUID())
                .roomId(dto.getRoomId())
                .userId(dto.getUserId())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .status(BookingStatus.CONFIRMED)
                .createdAt(LocalDateTime.now())
                .build();

        Booking saved = bookingRepository.save(booking);
        BookingResponseDTO response = new BookingResponseDTO(saved);

        dispatchEvents(response, "RESERVA_CRIADA");
        return response;
    }

    @Transactional
    public BookingResponseDTO rescheduleBooking(UUID id, LocalDateTime newStart, LocalDateTime newEnd) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reserva não encontrada."));

        validateDates(newStart, newEnd);

        // AQUI: Usa o novo método do repositório que ignora o ID da reserva atual
        boolean conflict = bookingRepository.existsConflictingBookingExcludingId(
                booking.getRoomId(), newStart, newEnd, id);

        if (conflict) {
            throw new BusinessException("Conflito: O novo horário escolhido já está ocupado por outra reserva.");
        }

        booking.setStartTime(newStart);
        booking.setEndTime(newEnd);

        Booking updated = bookingRepository.save(booking);
        BookingResponseDTO response = new BookingResponseDTO(updated);

        dispatchEvents(response, "RESERVA_REAGENDADA");
        return response;
    }

    @Transactional
    public void cancelBooking(UUID id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reserva não encontrada."));

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        try {
            auditPublisher.sendAuditEvent("RESERVA_CANCELADA", "ID: " + id);
        } catch (Exception e) {
            log.error("Erro ao enviar auditoria de cancelamento: {}", e.getMessage());
        }
    }

    @Transactional
    public void deleteBookingPermanently(UUID id) {
        if (!bookingRepository.existsById(id)) {
            throw new NotFoundException("Reserva não encontrada.");
        }
        bookingRepository.deleteById(id);

        try {
            auditPublisher.sendAuditEvent("RESERVA_EXCLUIDA", "ID: " + id);
        } catch (Exception e) {
            log.error("Erro ao enviar auditoria de exclusão: {}", e.getMessage());
        }
    }

    // --- MÉTODOS DE APOIO ---

    private void validateDates(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new BusinessException("Datas de início e fim são obrigatórias.");
        }
        if (start.isAfter(end) || start.isEqual(end)) {
            throw new BusinessException("A data de início deve ser anterior à data de fim.");
        }
        if (start.isBefore(LocalDateTime.now())) {
            throw new BusinessException("Não é permitido agendar reservas no passado.");
        }
    }

    private void validateRoomAndUser(UUID roomId, UUID userId) {
        try {
            roomClient.getRoomById(roomId);
        } catch (Exception e) {
            throw new BusinessException("Sala não encontrada ou serviço de salas fora do ar.");
        }
        try {
            userClient.getUserById(userId);
        } catch (Exception e) {
            throw new BusinessException("Usuário não encontrado ou serviço de usuários fora do ar.");
        }
    }

    private void dispatchEvents(BookingResponseDTO response, String action) {
        // Envio para RabbitMQ (Notificação)
        try {
            eventPublisher.sendReservationCreatedEvent(response);
        } catch (Exception e) {
            log.error("Falha ao comunicar com RabbitMQ: {}", e.getMessage());
        }

        // Envio para Kafka (Auditoria Detalhada)
        try {
            // Voltando a incluir o RoomId na mensagem para o banco de auditoria
            String details = String.format("Acao: %s | Reserva ID: %s | Sala ID: %s",
                    action, response.getId(), response.getRoomId());

            auditPublisher.sendAuditEvent(action, details);
            log.info("Log enviado ao Kafka: {}", details);
        } catch (Exception e) {
            log.error("Falha ao comunicar com Kafka: {}", e.getMessage());
        }
    }
}