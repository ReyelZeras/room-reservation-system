package com.example.reservation.controller;

import com.example.reservation.dto.SalaRequest;
import com.example.reservation.dto.SalaResponse;
import com.example.reservation.service.SalaService;
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
    public List<SalaResponse> listarTodas() {
        return salaService.listarTodas();
    }

    @GetMapping("/ativas")
    public List<SalaResponse> listarAtivas() {
        return salaService.listarAtivas();
    }

    @GetMapping("/{id}")
    public SalaResponse buscarPorId(@PathVariable UUID id) {
        return salaService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SalaResponse criar(@RequestBody @Valid SalaRequest request) {
        return salaService.criar(request);
    }

    @PutMapping("/{id}")
    public SalaResponse atualizar(@PathVariable UUID id,
                                  @RequestBody @Valid SalaRequest request) {
        return salaService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable UUID id) {
        salaService.remover(id);
    }
}

