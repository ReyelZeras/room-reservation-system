package com.example.reservation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de entrada para usuário.
 */
public record UsuarioRequest(
        @NotBlank String nome,
        @NotBlank @Email String email,
        String cargo
) {
}

