package com.example.reservation.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de entrada para criação de reserva.
 */
public record ReservaRequest(
        @NotNull(message = "O ID do usuário é obrigatório")
        UUID salaId,
        @NotNull(message = "O ID da sala é obrigatório")
        UUID usuarioId,
        @NotNull(message = "A data de início é obrigatória")
        @FutureOrPresent(message = "A reserva não pode ser no passado")
        @NotNull LocalDateTime dataHoraInicio,
        @NotNull(message = "A data de fim é obrigatória")
        @Future(message = "A data de fim deve ser uma data futura")
        LocalDateTime dataHoraFim
) {
}

