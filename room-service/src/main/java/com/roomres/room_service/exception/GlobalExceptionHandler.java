package com.roomres.room_service.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Arquitetura: Capturo erros estruturais do banco de dados (como a violação da restrição UNIQUE
    // na coluna 'name' da sala) e converto num erro 400 (Bad Request) limpo para o cliente.
    // Esta abordagem impede que um erro interno de banco de dados (500) vaze a stack trace para o frontend.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("erro", "Não foi possível salvar. Verifique se já existe uma sala cadastrada com este mesmo nome na nossa base.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}