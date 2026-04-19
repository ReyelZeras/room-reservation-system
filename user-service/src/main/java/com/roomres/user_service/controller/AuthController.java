package com.roomres.user_service.controller;

import com.roomres.user_service.security.CustomUserDetailsService;
import com.roomres.user_service.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints para gestão de sessão e login social")
public class AuthController {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Operation(summary = "Dados do usuário logado via GitHub")
    @GetMapping("/user")
    public Map<String, Object> user(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return Map.of("error", "Usuário não autenticado. Faça login via /oauth2/authorization/github");
        }
        return principal.getAttributes();
    }

    @Operation(summary = "Gerar token de desenvolvimento", description = "Gera um JWT válido para testes locais.")
    @GetMapping("/dev/token")
    public ResponseEntity<String> getDevToken(@RequestParam String email) {
        UserDetails user = userDetailsService.loadUserByUsername(email);
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(token);
    }
}