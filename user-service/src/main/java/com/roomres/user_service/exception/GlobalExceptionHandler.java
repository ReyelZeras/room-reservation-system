package com.roomres.user_service.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Collections;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "Violação de Regra de Negócio", ex.getMessage(), request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return buildError(HttpStatus.UNAUTHORIZED, "Não Autorizado", "E-mail ou senha incorretos.", request);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiError> handleUsernameNotFound(UsernameNotFoundException ex, HttpServletRequest request) {
        return buildError(HttpStatus.UNAUTHORIZED, "Não Autorizado", "E-mail ou senha incorretos.", request);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiError> handleDisabledException(DisabledException ex, HttpServletRequest request) {
        return buildError(HttpStatus.FORBIDDEN, "Conta Inativa", "A sua conta está inativa. Por favor, verifique a sua caixa de e-mail e clique no link de ativação.", request);
    }

    // NOVA PROTEÇÃO: Caso o Spring Security encapsule a exceção nativa
    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<ApiError> handleInternalAuth(InternalAuthenticationServiceException ex, HttpServletRequest request) {
        if (ex.getCause() instanceof DisabledException || ex.getMessage().contains("User is disabled")) {
            return buildError(HttpStatus.FORBIDDEN, "Conta Inativa", "A sua conta está inativa. Por favor, verifique a sua caixa de e-mail e clique no link de ativação.", request);
        }
        return buildError(HttpStatus.UNAUTHORIZED, "Não Autorizado", "E-mail ou senha incorretos.", request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, "Conflito de Dados", "O e-mail ou nome de usuário informado já existe.", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneralException(Exception ex, HttpServletRequest request) {
        ApiError error = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("Ocorreu um erro inesperado no servidor")
                .path(request.getRequestURI())
                .details(Collections.singletonList(ex.getMessage()))
                .build();
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiError> buildError(HttpStatus status, String errorStr, String message, HttpServletRequest request) {
        ApiError error = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(errorStr)
                .message(message)
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(error, status);
    }
}