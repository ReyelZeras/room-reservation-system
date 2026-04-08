package com.roomres.user_service.controller;

import com.roomres.user_service.dto.UserDTO;
import com.roomres.user_service.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;

    // Injetamos o UserService no Controller
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getMyProfile(@AuthenticationPrincipal OAuth2User principal) {
        // Retorna 401 Unauthorized se não houver sessão ativa
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        String providerId = Objects.toString(principal.getAttributes().get("id"), null);

        // Busca o utilizador no banco, converte para DTO e retorna 200 OK
        // Se não encontrar, retorna 404 Not Found
        return userService.findByProviderId(providerId)
                .map(user -> ResponseEntity.ok(new UserDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }
}