package com.example.reservation.exception;

import java.time.LocalDateTime;

/**
 * Payload padrão de erro para a API.
 */
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
}

