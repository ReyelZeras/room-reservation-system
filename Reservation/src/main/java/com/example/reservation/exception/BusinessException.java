package com.example.reservation.exception;

/**
 * Exceção de regra de negócio (HTTP 400).
 * Usada para representar validações de domínio violadas, como
 * conflitos de horário ou sala inativa.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}

