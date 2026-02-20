package com.example.reservation.dto;

import com.example.reservation.model.StatusSala;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de entrada para criação/atualização de sala.
 */
public record SalaRequest(
        @NotBlank String nome,
        @NotNull @Min(1) Integer capacidade,
        @NotNull StatusSala status,
        String localizacao
) {
}

