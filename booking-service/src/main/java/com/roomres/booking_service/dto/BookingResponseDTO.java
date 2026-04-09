package com.roomres.booking_service.dto;

import com.roomres.booking_service.model.Booking;
import com.roomres.booking_service.model.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDTO {
    private UUID id;
    private UUID roomId;
    private UUID userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BookingStatus status;
    private LocalDateTime createdAt;

    // Construtor manual para garantir o mapeamento correto da entidade para o DTO
    public BookingResponseDTO(Booking booking) {
        if (booking != null) {
            this.id = booking.getId();
            this.roomId = booking.getRoomId();
            this.userId = booking.getUserId();
            this.startTime = booking.getStartTime();
            this.endTime = booking.getEndTime();
            this.status = booking.getStatus();
            this.createdAt = booking.getCreatedAt();
        }
    }
}