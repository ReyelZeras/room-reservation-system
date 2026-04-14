package com.roomres.user_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints para gestão de sessão e login social")
public class AuthController {

    @Operation(summary = "Dados do usuário logado", description = "Retorna as informações do perfil obtidas do Provedor (ex: GitHub).")
    @GetMapping("/user")
    public Map<String, Object> user(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return Map.of("error", "Usuário não autenticado. Faça login via /oauth2/authorization/github");
        }
        return principal.getAttributes();
    }
}