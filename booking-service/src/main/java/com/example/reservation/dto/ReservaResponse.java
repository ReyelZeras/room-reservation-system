package com.example.reservation.dto;

import com.example.reservation.model.Reserva;
import com.example.reservation.model.StatusReserva;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de saída para reserva.
 */
public record ReservaResponse(
        UUID id,
        UUID salaId,
        UUID usuarioId,
        LocalDateTime dataHoraInicio,
        LocalDateTime dataHoraFim,
        StatusReserva status
) {

    public static ReservaResponse fromEntity(Reserva reserva) {
        return new ReservaResponse(
                reserva.getReservaID(),
                reserva.getSala().getSalaID(),
                reserva.getUsuario().getUserID(),
                reserva.getDataHoraInicio(),
                reserva.getDataHoraFim(),
                reserva.getStatus()
        );
    }
}

