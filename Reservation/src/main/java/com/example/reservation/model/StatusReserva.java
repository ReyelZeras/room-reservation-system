package com.example.reservation.model;

/**
 * Status possíveis de uma reserva.
 *
 * Transições permitidas:
 * - ATIVA -> CANCELADA (quando o usuário cancela a reserva);
 * - CANCELADA não volta para ATIVA (mantendo histórico consistente).
 */
public enum StatusReserva {
    ATIVA,
    CANCELADA
}
