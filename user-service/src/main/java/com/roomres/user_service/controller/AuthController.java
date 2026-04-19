package com.roomres.user_service.controller;

import com.roomres.user_service.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints para gestão de sessão e login social")
public class AuthController {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Operation(summary = "Gerar token de desenvolvimento", description = "Gera um JWT válido para testes locais.")
    @GetMapping("/dev/token")
    public ResponseEntity<String> getDevToken(@RequestParam String email) {
        UserDetails user = userDetailsService.loadUserByUsername(email);
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(token);
    }

    // CORREÇÃO: Endpoint /me adicionado para receber o redirecionamento pós-login do GitHub
    @Operation(summary = "Dados do usuário logado via GitHub")
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMyProfile(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam(required = false) String token) {

        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        // Retorna os dados do GitHub + o Token JWT gerado para facilitar a cópia no navegador!
        Map<String, Object> response = new HashMap<>(principal.getAttributes());
        if (token != null) {
            response.put("jwt_token_para_uso_no_postman", token);
        }

        return ResponseEntity.ok(response);
    }
}