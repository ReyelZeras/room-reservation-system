package com.example.reservation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de entrada para usuário.
 */
public record UsuarioRequest(
        @NotBlank(message = "O nome não pode estar em branco")
        String nome,
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Formato de e-mail inválido")
        String email,
        String cargo
) {
}

