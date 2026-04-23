package com.roomres.user_service.service;

import com.roomres.user_service.model.User;
import com.roomres.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // ==========================================
    // MÉTODOS CRUD (Adicionados para o Angular/Testes)
    // ==========================================

    @Transactional
    public User createUser(User user) {
        user.setProvider("local");
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER"); // Default
        }
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(UUID id, User userDetails) {
        return userRepository.findById(id).map(user -> {
            user.setName(userDetails.getName());
            user.setEmail(userDetails.getEmail());
            // Atualiza a ROLE com segurança
            if (userDetails.getRole() != null) {
                user.setRole(userDetails.getRole().toUpperCase());
            }
            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
    }

    @Transactional
    public void deleteById(UUID id) {
        userRepository.deleteById(id);
    }

    // ==========================================
    // MÉTODOS OAUTH2 E TESTES RESTAURADOS
    // ==========================================

    // Método vital para o UserServiceTest passar!
    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    // Método vital para a compilação do CustomOAuth2UserService
    @Transactional
    public User processOAuthUser(String email, String login, String name, String providerId) {
        return userRepository.findByEmail(email)
                .map(user -> {
                    user.setName(name);
                    return userRepository.save(user);
                })
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .username(login != null ? login : email) // Fallback caso o github não mande login
                            .email(email)
                            .name(name)
                            .provider("github")
                            .providerId(providerId)
                            .role("USER")
                            .build();
                    return userRepository.save(newUser);
                });
    }
}