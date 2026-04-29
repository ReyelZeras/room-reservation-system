package com.roomres.booking_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestDTO {
    private UUID roomId;
    private UUID userId;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}