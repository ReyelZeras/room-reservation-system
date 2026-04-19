package com.roomres.user_service.controller;

import com.roomres.user_service.model.User;
import com.roomres.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints CRUD para gerenciamento de usuários")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Ping de saúde", description = "Verifica se o serviço de usuários está respondendo.")
    @GetMapping("/health/ping")
    public String ping() {
        return "User Service is UP and Running!";
    }

    // CORREÇÃO: Rota que lista todos os usuários estava faltando!
    @Operation(summary = "Lista todos os usuários")
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    // CORREÇÃO: Rota que busca por ID estava faltando! É vital para o BookingService validar o usuário!
    @Operation(summary = "Busca usuário por ID")
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Atualiza um usuário")
    @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso")
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable UUID id, @RequestBody User userDetails) {
        return userService.findById(id).map(user -> {
            user.setName(userDetails.getName());
            user.setEmail(userDetails.getEmail());
            return ResponseEntity.ok(userService.save(user));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Deletar usuário")
    @ApiResponse(responseCode = "204", description = "Usuário removido com sucesso")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}