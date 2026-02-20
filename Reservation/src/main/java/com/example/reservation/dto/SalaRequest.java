package com.example.reservation.dto;

import com.example.reservation.model.StatusSala;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de entrada para criação/atualização de sala.
 */
public record SalaRequest(
        @NotBlank(message = "O nome não pode estar em branco")
        String nome,
        @NotNull (message = "A capacidade não pode estar em branco")
        @Min(1)
        Integer capacidade,
        @NotNull (message = "O status não pode estar em branco")
        StatusSala status,
        String localizacao
) {
}

