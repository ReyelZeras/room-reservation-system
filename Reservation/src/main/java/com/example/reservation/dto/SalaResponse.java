package com.example.reservation.dto;

import com.example.reservation.model.Sala;
import com.example.reservation.model.StatusSala;

import java.util.UUID;

/**
 * DTO de saída para sala.
 */
public record SalaResponse(
        UUID id,
        String nome,
        Integer capacidade,
        StatusSala status,
        String localizacao
) {

    public static SalaResponse fromEntity(Sala sala) {
        return new SalaResponse(
                sala.getSalaID(),
                sala.getNome(),
                sala.getCapacidade(),
                sala.getStatus(),
                sala.getLocalizacao()
        );
    }
}

