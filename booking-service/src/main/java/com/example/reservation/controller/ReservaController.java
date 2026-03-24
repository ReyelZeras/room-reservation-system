package com.example.reservation.controller;

import com.example.reservation.dto.ReservaRequest;
import com.example.reservation.dto.ReservaResponse;
import com.example.reservation.service.ReservaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservas")
@Tag(name = "Reservas", description = "Endpoints para gestão de reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping
    @Operation(summary = "Busca todas as reservas", description = "Busca todas as reservas e retorna para o usuário")
    public Page<ReservaResponse> listar(Pageable pageable) {
        return reservaService.listar(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma reserva especifica", description = "Verifica por ID se a reserva existe e a retorna para o usuário")
    public ReservaResponse buscarPorId(@PathVariable UUID id) {
        return reservaService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma nova reserva", description = "Verifica disponibilidade e cria reserva")
    public ReservaResponse criar(@RequestBody @Valid ReservaRequest request) {
        return reservaService.criar(request);
    }

    @PostMapping("/{id}/cancelamento")
    @Operation(summary = "Cancela uma reserva", description = "Verifica a existência e cancela a reserva")
    public ReservaResponse cancelar(@PathVariable @Valid UUID id) {
        return reservaService.cancelar(id);
    }
}

