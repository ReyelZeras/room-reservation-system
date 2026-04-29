package com.roomres.booking_service.service;

import com.roomres.booking_service.client.RoomClient;
import com.roomres.booking_service.client.UserClient;
import com.roomres.booking_service.dto.BookingRequestDTO;
import com.roomres.booking_service.dto.BookingResponseDTO;
import com.roomres.booking_service.dto.RoomDTO;
import com.roomres.booking_service.exception.BusinessException;
import com.roomres.booking_service.exception.NotFoundException;
import com.roomres.booking_service.model.Booking;
import com.roomres.booking_service.model.BookingStatus;
import com.roomres.booking_service.publisher.AuditPublisher;
import com.roomres.booking_service.publisher.BookingEventPublisher;
import com.roomres.booking_service.repository.BookingRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    // Busca os dados enriquecidos do Usuário
    private Map<String, String> getUserDetails(UUID userId) {
        @SuppressWarnings("unchecked")
        Map<String, Object> user = (Map<String, Object>) userClient.getUserById(userId);
        return Map.of(
                "email", (String) user.get("email"),
                "name", (String) user.get("name")
        );
    }

    // Busca os dados enriquecidos da Sala
    private String getRoomName(UUID roomId) {
        @SuppressWarnings("unchecked")
        Map<String, Object> room = (Map<String, Object>) roomClient.getRoomById(roomId);
        return (String) room.get("name");
    }

    @Transactional(readOnly = true)
    @CircuitBreaker(name = "roomServiceCB", fallbackMethod = "fallbackGetAvailableRooms")
    public List<RoomDTO> getAvailableRooms(LocalDateTime start, LocalDateTime end) {
        validateDates(start, end);
        List<RoomDTO> allRooms = roomClient.getAllRooms();
        List<Booking> conflictingBookings = bookingRepository.findConflictingBookings(start, end);
        Set<UUID> occupiedRoomIds = conflictingBookings.stream().map(Booking::getRoomId).collect(Collectors.toSet());

        return allRooms.stream()
                .filter(room -> !occupiedRoomIds.contains(room.id()))
                .filter(room -> "AVAILABLE".equals(room.status()))
                .collect(Collectors.toList());
    }

    public List<RoomDTO> fallbackGetAvailableRooms(LocalDateTime start, LocalDateTime end, Throwable t) {
        if (t instanceof BusinessException) throw (BusinessException) t;
        log.error("Circuit Breaker ativado na pesquisa de disponibilidade: {}", t.getMessage());
        throw new BusinessException("O serviço de catálogo de salas está temporariamente indisponível.");
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getAll() {
        return bookingRepository.findAll().stream().map(BookingResponseDTO::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BookingResponseDTO getById(UUID id) {
        return new BookingResponseDTO(bookingRepository.findById(id).orElseThrow(() -> new NotFoundException("Reserva não encontrada.")));
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getByUserId(UUID userId) {
        return bookingRepository.findByUserId(userId).stream().map(BookingResponseDTO::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getByRoomId(UUID roomId) {
        return bookingRepository.findByRoomId(roomId).stream().map(BookingResponseDTO::new).collect(Collectors.toList());
    }

    @Transactional
    @CircuitBreaker(name = "roomServiceCB", fallbackMethod = "fallbackCreateBooking")
    public BookingResponseDTO createBooking(BookingRequestDTO dto) {
        log.info("Iniciando criação de reserva...");
        validateDates(dto.getStartTime(), dto.getEndTime());

        if (bookingRepository.existsConflictingBooking(dto.getRoomId(), dto.getStartTime(), dto.getEndTime())) {
            throw new BusinessException("A sala já está reservada para o período selecionado.");
        }

        Map<String, String> userDetails = getUserDetails(dto.getUserId());
        String roomName = getRoomName(dto.getRoomId());

        Booking booking = Booking.builder()
                .id(UUID.randomUUID())
                .roomId(dto.getRoomId())
                .userId(dto.getUserId())
                // 🚀 A CORREÇÃO ESTÁ AQUI: Passamos o título do DTO para a Entidade
                .title(dto.getTitle())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .status(BookingStatus.CONFIRMED)
                .build();

        BookingResponseDTO response = new BookingResponseDTO(bookingRepository.save(booking));

        dispatchEvents(response, "RESERVA_CRIADA", userDetails.get("email"), userDetails.get("name"), roomName);

        return response;
    }

    public BookingResponseDTO fallbackCreateBooking(BookingRequestDTO dto, Throwable t) {
        if (t instanceof BusinessException) throw (BusinessException) t;
        if (t instanceof feign.FeignException && ((feign.FeignException) t).status() == 404) {
            throw new NotFoundException("O ID da Sala ou do Usuário informado não existe na nossa base de dados.");
        }
        log.error("CIRCUIT BREAKER ATIVADO! Motivo da falha: {}", t.getMessage());
        throw new BusinessException("Os sistemas de validação estão temporariamente indisponíveis.");
    }

    @Transactional
    public BookingResponseDTO rescheduleBooking(UUID id, LocalDateTime newStart, LocalDateTime newEnd) {
        Booking booking = bookingRepository.findById(id).orElseThrow(() -> new NotFoundException("Reserva não encontrada."));
        validateDates(newStart, newEnd);

        if (bookingRepository.existsConflictingBookingExcludingId(booking.getRoomId(), newStart, newEnd, id)) {
            throw new BusinessException("Conflito: O novo horário escolhido já está ocupado.");
        }

        Map<String, String> userDetails = getUserDetails(booking.getUserId());
        String roomName = getRoomName(booking.getRoomId());

        booking.setStartTime(newStart);
        booking.setEndTime(newEnd);

        BookingResponseDTO response = new BookingResponseDTO(bookingRepository.save(booking));
        dispatchEvents(response, "RESERVA_REAGENDADA", userDetails.get("email"), userDetails.get("name"), roomName);

        return response;
    }

    @Transactional
    public void cancelBooking(UUID id) {
        Booking booking = bookingRepository.findById(id).orElseThrow(() -> new NotFoundException("Reserva não encontrada."));
        booking.setStatus(BookingStatus.CANCELLED);

        Map<String, String> userDetails = getUserDetails(booking.getUserId());
        String roomName = getRoomName(booking.getRoomId());

        dispatchEvents(new BookingResponseDTO(bookingRepository.save(booking)), "RESERVA_CANCELADA", userDetails.get("email"), userDetails.get("name"), roomName);
    }

    @Transactional
    public void deleteBookingPermanently(UUID id) {
        Booking booking = bookingRepository.findById(id).orElseThrow(() -> new NotFoundException("Reserva não encontrada."));
        bookingRepository.delete(booking);
        try {
            auditPublisher.sendAuditEvent("RESERVA_DELETADA", "Reserva apagada.");
        } catch (Exception e) {}
    }

    private void validateDates(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) throw new BusinessException("Datas obrigatórias.");
        if (start.isAfter(end) || start.isEqual(end)) throw new BusinessException("Início deve ser anterior ao fim.");
        if (start.isBefore(LocalDateTime.now())) throw new BusinessException("Não é permitido agendar no passado.");
    }

    private void dispatchEvents(BookingResponseDTO response, String action, String userEmail, String userName, String roomName) {
        try {
            eventPublisher.sendReservationCreatedEvent(response, userEmail, userName, roomName);
        } catch (Exception e) {
            log.error("Erro RabbitMQ: {}", e.getMessage());
        }
        try {
            auditPublisher.sendAuditEvent(action, "Reserva ID: " + response.getId());
        } catch (Exception e) {
            log.error("Erro Kafka: {}", e.getMessage());
        }
    }
}