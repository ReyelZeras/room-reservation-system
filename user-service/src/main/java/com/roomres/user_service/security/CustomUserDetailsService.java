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

        // CORREÇÃO DEFINITIVA: Passa a senha real do banco de dados (Hash BCrypt) para o Spring Security comparar!
        String rolePrefix = user.getRole() != null ? user.getRole().toUpperCase() : "USER";

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword() != null ? user.getPassword() : "", // Senha real do BD!
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + rolePrefix))
        );
    }
}