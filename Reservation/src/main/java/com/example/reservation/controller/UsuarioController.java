package com.example.reservation.controller;

import com.example.reservation.dto.UsuarioRequest;
import com.example.reservation.dto.UsuarioResponse;
import com.example.reservation.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioResponse> listarTodos() {
        return usuarioService.listarTodos();
    }

    @GetMapping("/{id}")
    public UsuarioResponse buscarPorId(@PathVariable UUID id) {
        return usuarioService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse criar(@RequestBody @Valid UsuarioRequest request) {
        return usuarioService.criar(request);
    }

    @PutMapping("/{id}")
    public UsuarioResponse atualizar(@PathVariable UUID id,
                                     @RequestBody @Valid UsuarioRequest request) {
        return usuarioService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable UUID id) {
        usuarioService.remover(id);
    }
}

