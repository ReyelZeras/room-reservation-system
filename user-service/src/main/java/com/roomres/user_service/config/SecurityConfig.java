package com.roomres.user_service.config;

import com.roomres.user_service.model.User;
import com.roomres.user_service.repository.UserRepository;
import com.roomres.user_service.security.JwtAuthenticationFilter;
import com.roomres.user_service.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Optional;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository; // NOVO: Injetado para fazermos o Auto-Registro

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/api/v1/users/register",
                                "/v2/api-docs",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-resources",
                                "/swagger-resources/**",
                                "/configuration/ui",
                                "/configuration/security",
                                "/swagger-ui/**",
                                "/webjars/**",
                                "/swagger-ui.html",
                                "/api/v1/users/**",
                                "/login/**",
                                "/oauth2/**",
                                "/error"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler((request, response, authentication) -> {
                            OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
                            String login = oauthUser.getAttribute("login");
                            String email = oauthUser.getAttribute("email");
                            String name = oauthUser.getAttribute("name");

                            // Fallbacks caso o GitHub não envie alguns dados
                            if (email == null) email = login + "@github.com";
                            if (name == null) name = login;

                            // 1. Busca Inteligente (Tenta pelo email real, depois pelo bug legado)
                            Optional<User> userOpt = userRepository.findByEmail(email);
                            if (userOpt.isEmpty()) {
                                userOpt = userRepository.findByEmail(login);
                            }
                            if (userOpt.isEmpty()) {
                                userOpt = userRepository.findByUsername(email);
                            }

                            User user;
                            if (userOpt.isEmpty()) {
                                // 2. Auto-Registro de novos usuários via GitHub
                                user = new User();
                                user.setEmail(email);
                                user.setUsername(email);
                                user.setName(name);
                                user.setRole("USER");
                                user.setProvider("github");
                                if (oauthUser.getAttribute("id") != null) {
                                    user.setProviderId(oauthUser.getAttribute("id").toString());
                                }
                                user = userRepository.save(user);
                            } else {
                                // 3. Auto-Correção (Conserta o email sem @ do banco legado se existir)
                                user = userOpt.get();
                                if (user.getEmail() != null && !user.getEmail().contains("@")) {
                                    user.setEmail(email);
                                    userRepository.save(user);
                                }
                            }

                            // 4. Gera o Token com os dados consolidados e volta pro Angular
                            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
                            String token = jwtService.generateToken(userDetails);

                            response.sendRedirect("http://localhost:4200/login?token=" + token);
                        })
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}