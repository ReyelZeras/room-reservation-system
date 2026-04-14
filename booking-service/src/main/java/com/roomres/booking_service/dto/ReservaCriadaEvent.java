package com.roomres.booking_service.dto;

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
public class ReservaCriadaEvent {
    private UUID bookingId;
    private UUID roomId;
    private UUID userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
}