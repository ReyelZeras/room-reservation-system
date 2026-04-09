package com.roomres.user_service.security;

import com.roomres.user_service.model.User;
import com.roomres.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o e-mail: " + email));

        // Aqui você mapeia a role do seu User (se tiver) para as authorities do Spring Security
        // Assumindo um fluxo básico onde todos são ROLE_USER por padrão
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                "", // Senha vazia pois a autenticação é via OAuth2
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}