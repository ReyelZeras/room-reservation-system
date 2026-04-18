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

    // NOVO MÉTODO: Orquestração para descobrir salas disponíveis
    // Optei por cruzar os dados em memória aqui no orchestrator para não acoplar a base de dados do room-service às reservas.
    @Transactional(readOnly = true)
    @CircuitBreaker(name = "roomServiceCB", fallbackMethod = "fallbackGetAvailableRooms")
    public List<RoomDTO> getAvailableRooms(LocalDateTime start, LocalDateTime end) {
        validateDates(start, end);

        // 1. Pede todas as salas ao room-service (protegido pelo Circuit Breaker)
        List<RoomDTO> allRooms = roomClient.getAllRooms();

        // 2. Vai à própria BD buscar apenas as reservas que se sobrepõem às datas solicitadas
        List<Booking> conflictingBookings = bookingRepository.findConflictingBookings(start, end);

        // 3. Extrai os UUIDs das salas que estão ocupadas para uma pesquisa (Set é O(1) de performance)
        Set<UUID> occupiedRoomIds = conflictingBookings.stream()
                .map(Booking::getRoomId)
                .collect(Collectors.toSet());

        // 4. Filtra a lista de salas mantendo apenas as que NÃO estão no Set de ocupadas e que estão operacionais.
        return allRooms.stream()
                .filter(room -> !occupiedRoomIds.contains(room.id()))
                .filter(room -> "AVAILABLE".equals(room.status()))
                .collect(Collectors.toList());
    }

    // Fallback: Se o room-service estiver em baixo, devolve erro amigável em vez de um timeout de 500ms
    public List<RoomDTO> fallbackGetAvailableRooms(LocalDateTime start, LocalDateTime end, Throwable t) {
        log.error("Circuit Breaker ativado na pesquisa de disponibilidade: {}", t.getMessage());
        throw new BusinessException("O serviço de catálogo de salas está temporariamente indisponível. Tente novamente em instantes.");
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getAll() {
        return bookingRepository.findAll().stream().map(BookingResponseDTO::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BookingResponseDTO getById(UUID id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reserva não encontrada."));
        return new BookingResponseDTO(booking);
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

        // Usa o método do repositório para verificar conflitos
        if (bookingRepository.existsConflictingBooking(dto.getRoomId(), dto.getStartTime(), dto.getEndTime())) {
            throw new BusinessException("A sala já está reservada para o período selecionado.");
        }

        // Valida nos outros microsserviços via Feign Client (ponto monitorado pelo Circuit Breaker)
        validateRoomAndUser(dto.getRoomId(), dto.getUserId());

        Booking booking = Booking.builder()
                .id(UUID.randomUUID())
                .roomId(dto.getRoomId())
                .userId(dto.getUserId())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .status(BookingStatus.CONFIRMED)
                .build();

        Booking saved = bookingRepository.save(booking);
        BookingResponseDTO response = new BookingResponseDTO(saved);

        dispatchEvents(response, "RESERVA_CRIADA");

        return response;
    }

    // MÉTODO DE FALLBACK: Executado se o 'validateRoomAndUser' falhar por indisponibilidade de rede
    public BookingResponseDTO fallbackCreateBooking(BookingRequestDTO dto, Throwable t) {
        log.error("CIRCUIT BREAKER ATIVADO! Motivo da falha: {}", t.getMessage());
        throw new BusinessException("Os sistemas de validação de salas/usuários estão temporariamente indisponíveis. Tente novamente em instantes.");
    }

    @Transactional
    public BookingResponseDTO rescheduleBooking(UUID id, LocalDateTime newStart, LocalDateTime newEnd) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reserva não encontrada."));

        validateDates(newStart, newEnd);

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
        Booking updated = bookingRepository.save(booking);

        dispatchEvents(new BookingResponseDTO(updated), "RESERVA_CANCELADA");
    }

    @Transactional
    public void deleteBookingPermanently(UUID id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reserva não encontrada."));

        bookingRepository.delete(booking);

        try {
            auditPublisher.sendAuditEvent("RESERVA_DELETADA", "Reserva ID: " + id + " foi apagada permanentemente.");
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
        try {
            eventPublisher.sendReservationCreatedEvent(response);
        } catch (Exception e) {
            log.error("Erro ao publicar evento no RabbitMQ: {}", e.getMessage());
        }

        try {
            String details = String.format("Reserva ID: %s | Sala ID: %s | Usuário ID: %s",
                    response.getId(), response.getRoomId(), response.getUserId());
            auditPublisher.sendAuditEvent(action, details);
            log.info("Log enviado ao Kafka: {}", details);
        } catch (Exception e) {
            log.error("Falha ao comunicar com Kafka: {}", e.getMessage());
        }
    }
}