package com.roomres.user_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    // Arquitetura: Este filtro intercepta CADA requisição que entra no serviço de utilizadores.
    // Optei por OncePerRequestFilter em vez do Filter genérico para garantir que a validação não ocorre em loop interno.
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Fast-fail: Se não tem cabeçalho ou não começa por 'Bearer ', ignora e passa à frente.
        // O Spring Security tratará de bloquear a rota mais tarde se for uma rota protegida.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            jwt = authHeader.substring(7); // Remove a palavra "Bearer "
            userEmail = jwtService.extractUsername(jwt);

            // Se conseguimos ler o email e o contexto de segurança atual está vazio (o utilizador não está autenticado nesta thread)...
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Vamos à base de dados carregar o perfil para verificar roles/authorities
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // Validação criptográfica do token em relação ao utilizador
                if (jwtService.isTokenValid(jwt, userDetails)) {

                    // Constrói o "Crachá" de autenticação oficial do Spring Security
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Injeta a autenticação no contexto da Thread atual. A partir daqui, os @Controllers
                    // conseguem aceder ao utilizador com a anotação @AuthenticationPrincipal.
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Em caso de erro (token expirado ou forjado), a resposta falhará silenciosamente no filtro
            // e resultará num 403/401 natural pelo framework.
        }

        filterChain.doFilter(request, response);
    }
}