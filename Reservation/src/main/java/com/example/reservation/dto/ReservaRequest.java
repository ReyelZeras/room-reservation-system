package com.example.reservation.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de entrada para criação de reserva.
 */
public record ReservaRequest(
        @NotNull UUID salaId,
        @NotNull UUID usuarioId,
        @NotNull LocalDateTime dataHoraInicio,
        @NotNull LocalDateTime dataHoraFim
) {
}

