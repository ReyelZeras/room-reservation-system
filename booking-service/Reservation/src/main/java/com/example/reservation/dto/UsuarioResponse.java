package com.example.reservation.dto;

import com.example.reservation.model.Usuario;

import java.util.UUID;

/**
 * DTO de saída para usuário.
 */
public record UsuarioResponse(
        UUID id,
        String nome,
        String email,
        String cargo
) {

    public static UsuarioResponse fromEntity(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getUserID(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getCargo()
        );
    }
}

