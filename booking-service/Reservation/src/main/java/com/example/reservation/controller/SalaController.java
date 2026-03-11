package com.example.reservation.controller;

import com.example.reservation.dto.SalaRequest;
import com.example.reservation.dto.SalaResponse;
import com.example.reservation.service.SalaService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/salas")
public class SalaController {

    private final SalaService salaService;

    public SalaController(SalaService salaService) {
        this.salaService = salaService;
    }

    @GetMapping
    @Operation(summary = "Busca todas as salas", description = "Busca todas as salas e retorna para o usuário")
    public List<SalaResponse> listarTodas() {
        return salaService.listarTodas();
    }

    @GetMapping("/ativas")
    @Operation(summary = "Busca todas as salas ativas", description = "Busca todas as salas com o status ATIVA e retorna para o usuário")
    public List<SalaResponse> listarAtivas() {
        return salaService.listarAtivas();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma sala especifica", description = "Verifica por ID se a sala existe e a retorna para o usuário")
    public SalaResponse buscarPorId(@PathVariable UUID id) {
        return salaService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma nova sala", description = "Cria uma nova sala")
    public SalaResponse criar(@RequestBody @Valid SalaRequest request) {
        return salaService.criar(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma sala", description = "Verifica a existência da sala e atualiza algum parâmetro dela")
    public SalaResponse atualizar(@PathVariable UUID id,
                                  @RequestBody @Valid SalaRequest request) {
        return salaService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Apaga uma sala", description = "Verifica a existência e apaga uma sala")
    public void remover(@PathVariable UUID id) {
        salaService.remover(id);
    }
}

