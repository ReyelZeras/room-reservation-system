package com.roomres.user_service.controller;

import com.roomres.user_service.model.User;
import com.roomres.user_service.repository.UserRepository;
import com.roomres.user_service.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints para gestão de sessão e login social")
public class AuthController {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository; // Injetado para buscar o perfil completo na base de dados

    @Operation(summary = "Gerar token de desenvolvimento", description = "Gera um JWT válido para testes locais.")
    @GetMapping("/dev/token")
    public ResponseEntity<String> getDevToken(@RequestParam String email) {
        UserDetails user = userDetailsService.loadUserByUsername(email);
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(token);
    }

    @Operation(summary = "Dados do usuário logado", description = "Retorna o perfil completo do usuário que está no contexto de segurança atual.")
    @GetMapping("/me")
    public ResponseEntity<User> getMyProfile(Authentication authentication) {

        // 1. Verifica se existe uma autenticação válida na requisição
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String email = null;
        Object principal = authentication.getPrincipal();

        // 2. Cenário A: O utilizador logou-se via GitHub (OAuth2)
        if (principal instanceof OAuth2User oAuth2User) {
            email = oAuth2User.getAttribute("email");
            if (email == null) {
                email = oAuth2User.getAttribute("login") + "@github.com"; // Fallback do GitHub
            }
        }
        // 3. Cenário B: O utilizador logou-se via Angular / Formulário Local (JWT / UserDetails)
        else if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        }

        // 4. Se não conseguiu extrair o e-mail de nenhuma forma, bloqueia
        if (email == null) {
            return ResponseEntity.status(401).build();
        }

        // 5. Busca o objeto User completo na base de dados e retorna em JSON
        Optional<User> userOpt = userRepository.findByEmail(email);

        return userOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(401).build());
    }
}