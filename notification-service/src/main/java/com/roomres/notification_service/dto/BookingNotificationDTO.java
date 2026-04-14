package com.roomres.notification_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // REGRA DE OURO: Ignora campos desconhecidos no JSON
public class BookingNotificationDTO {
    private UUID id;
    private UUID roomId;
    private UUID userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private LocalDateTime createdAt; // Adicionado para espelhar perfeitamente
}