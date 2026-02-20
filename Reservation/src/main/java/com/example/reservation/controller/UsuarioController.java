package com.example.reservation.controller;

import com.example.reservation.dto.UsuarioRequest;
import com.example.reservation.dto.UsuarioResponse;
import com.example.reservation.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "Busca todos os usuários", description = "Busca todos os usuários e retorna")
    public List<UsuarioResponse> listarTodos() {
        return usuarioService.listarTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um usuário especifica", description = "Verifica por ID se o usuário existe e o retorna")

    public UsuarioResponse buscarPorId(@PathVariable UUID id) {
        return usuarioService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma novo usuário", description = "Cria uma novo usuário")
    public UsuarioResponse criar(@RequestBody @Valid UsuarioRequest request) {
        return usuarioService.criar(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um usuário", description = "Verifica a existência do usuário e atualiza algum parâmetro")
    public UsuarioResponse atualizar(@PathVariable UUID id,
                                     @RequestBody @Valid UsuarioRequest request) {
        return usuarioService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Apaga um usuário", description = "Verifica a existência e remove um usuário do sistema")
    public void remover(@PathVariable UUID id) {
        usuarioService.remover(id);
    }
}

