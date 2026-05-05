package com.roomres.booking_service;

import com.roomres.booking_service.client.RoomClient;
import com.roomres.booking_service.client.UserClient;
import com.roomres.booking_service.dto.BookingRequestDTO;
import com.roomres.booking_service.dto.BookingResponseDTO;
import com.roomres.booking_service.exception.BusinessException;
import com.roomres.booking_service.exception.NotFoundException;
import com.roomres.booking_service.model.Booking;
import com.roomres.booking_service.model.BookingStatus;
import com.roomres.booking_service.publisher.AuditPublisher;
import com.roomres.booking_service.publisher.BookingEventPublisher;
import com.roomres.booking_service.repository.BookingRepository;
import com.roomres.booking_service.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private RoomClient roomClient;
    @Mock private UserClient userClient;
    @Mock private BookingEventPublisher eventPublisher;
    @Mock private AuditPublisher auditPublisher;

    @InjectMocks
    private BookingService bookingService;

    private BookingRequestDTO requestDTO;
    private Booking bookingEntity;
    private UUID bookingId;

    @BeforeEach
    void setUp() {
        bookingId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        requestDTO = new BookingRequestDTO();
        requestDTO.setRoomId(roomId);
        requestDTO.setTitle("Reunião Estratégica");
        requestDTO.setStartTime(LocalDateTime.now().plusDays(1).withHour(10));
        requestDTO.setEndTime(LocalDateTime.now().plusDays(1).withHour(12));

        bookingEntity = new Booking();
        bookingEntity.setId(bookingId);
        bookingEntity.setRoomId(roomId);
        bookingEntity.setUserId(userId);
        bookingEntity.setTitle("Reunião Estratégica");
        bookingEntity.setStartTime(requestDTO.getStartTime());
        bookingEntity.setEndTime(requestDTO.getEndTime());
        bookingEntity.setStatus(BookingStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Deve criar uma reserva com sucesso e emitir eventos")
    void createBooking_Success() {
        when(bookingRepository.existsConflictingBooking(any(), any(), any())).thenReturn(false);

        Map<String, Object> mockUser = new HashMap<>();
        mockUser.put("email", "admin@roomres.com");
        mockUser.put("name", "Admin User");
        Map<String, Object> mockRoom = new HashMap<>();
        mockRoom.put("name", "Sala Presidencial");

        when(userClient.getUserById(any())).thenReturn(mockUser);
        when(roomClient.getRoomById(any())).thenReturn(mockRoom);
        when(bookingRepository.save(any(Booking.class))).thenReturn(bookingEntity);

        BookingResponseDTO response = bookingService.createBooking(requestDTO);

        assertNotNull(response);
        assertEquals(BookingStatus.CONFIRMED, response.getStatus());
    }

    @Test
    @DisplayName("Deve bloquear criação de reserva se houver conflito de horário")
    void createBooking_Conflict_ThrowsException() {
        when(bookingRepository.existsConflictingBooking(any(), any(), any())).thenReturn(true);
        assertThrows(BusinessException.class, () -> bookingService.createBooking(requestDTO));
    }

    @Test
    @DisplayName("Deve remarcar reserva com sucesso")
    void rescheduleBooking_Success() {
        LocalDateTime newStart = LocalDateTime.now().plusDays(2).withHour(14);
        LocalDateTime newEnd = LocalDateTime.now().plusDays(2).withHour(16);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(bookingEntity));
        when(bookingRepository.existsConflictingBookingExcludingId(any(), any(), any(), any())).thenReturn(false);

        Map<String, Object> mockUser = new HashMap<>();
        mockUser.put("email", "admin@roomres.com");
        mockUser.put("name", "Admin User");
        Map<String, Object> mockRoom = new HashMap<>();
        mockRoom.put("name", "Sala Presidencial");

        when(userClient.getUserById(any())).thenReturn(mockUser);
        when(roomClient.getRoomById(any())).thenReturn(mockRoom);
        when(bookingRepository.save(any(Booking.class))).thenReturn(bookingEntity);

        BookingResponseDTO response = bookingService.rescheduleBooking(bookingId, newStart, newEnd);
        assertNotNull(response);
    }

    @Test
    @DisplayName("Deve lançar erro ao remarcar com conflito")
    void rescheduleBooking_Conflict_ThrowsException() {
        LocalDateTime newStart = LocalDateTime.now().plusDays(2);
        LocalDateTime newEnd = LocalDateTime.now().plusDays(2).plusHours(1);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(bookingEntity));
        when(bookingRepository.existsConflictingBookingExcludingId(any(), any(), any(), any())).thenReturn(true);

        assertThrows(BusinessException.class, () -> bookingService.rescheduleBooking(bookingId, newStart, newEnd));
    }

    @Test
    @DisplayName("Deve lançar erro ao tentar cancelar reserva inexistente")
    void cancelBooking_NotFound_ThrowsException() {
        when(bookingRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> bookingService.cancelBooking(UUID.randomUUID()));
    }
}