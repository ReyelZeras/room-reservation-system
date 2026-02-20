package com.example.reservation.model;

/**
 * Status possíveis de uma sala.
 *
 * - ATIVA: pode receber novas reservas;
 * - INATIVA: não aceita novas reservas, mas reservas antigas são mantidas
 *   apenas para histórico.
 */
public enum StatusSala {
    ATIVA,
    INATIVA
}
