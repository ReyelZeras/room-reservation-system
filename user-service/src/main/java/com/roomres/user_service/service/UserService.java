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

    // ADICIONADO: Listar todos os usuários para o CRUD
    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    // Necessário para o AuthController (getMyProfile)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // ADICIONADO: Método genérico para salvar atualizações do CRUD
    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    // ADICIONADO: Método para deletar um usuário
    @Transactional
    public void deleteById(UUID id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public User processOAuthUser(String login, String email, String name, String providerId) {
        return userRepository.findByProviderId(providerId)
                .map(user -> {
                    user.setName(name);
                    return userRepository.save(user);
                })
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .username(login)
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