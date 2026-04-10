package com.roomres.user_service.controller;

import com.roomres.user_service.model.User;
import com.roomres.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users") // Certifica-te que não há espaços aqui
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Endpoint para testar se o Controller está ativo
    @GetMapping("/ping")
    public String ping() {
        return "User Service is UP";
    }
}