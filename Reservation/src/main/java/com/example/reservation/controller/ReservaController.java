package com.example.reservation.controller;

import com.example.reservation.dto.ReservaRequest;
import com.example.reservation.dto.ReservaResponse;
import com.example.reservation.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping
    public Page<ReservaResponse> listar(Pageable pageable) {
        return reservaService.listar(pageable);
    }

    @GetMapping("/{id}")
    public ReservaResponse buscarPorId(@PathVariable UUID id) {
        return reservaService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaResponse criar(@RequestBody @Valid ReservaRequest request) {
        return reservaService.criar(request);
    }

    @PostMapping("/{id}/cancelamento")
    public ReservaResponse cancelar(@PathVariable UUID id) {
        return reservaService.cancelar(id);
    }
}

