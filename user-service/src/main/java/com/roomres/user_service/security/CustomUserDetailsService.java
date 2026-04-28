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
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));

        String rolePrefix = user.getRole() != null ? user.getRole().toUpperCase() : "USER";


        // Ao passar 'user.isActive()' no 3º parâmetro (enabled), o próprio Spring Security
        // bloqueará o login e atirará a DisabledException de forma limpa e nativa!
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword() != null ? user.getPassword() : "",
                user.isActive(), // enabled
                true,            // accountNonExpired
                true,            // credentialsNonExpired
                true,            // accountNonLocked
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + rolePrefix))
        );
    }
}