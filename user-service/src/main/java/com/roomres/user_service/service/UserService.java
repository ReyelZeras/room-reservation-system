package com.roomres.user_service.service;

import com.roomres.user_service.model.User;
import com.roomres.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    // ADICIONADO: Necessário para o AuthController (getMyProfile)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public User processOAuthUser(String login, String email, String name, String providerId) {
        return userRepository.findByProviderId(providerId)
                .map(user -> {
                    user.setName(name);
                    user.setEmail(email);
                    return userRepository.save(user);
                })
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setId(UUID.randomUUID());
                    newUser.setUsername(login);
                    newUser.setEmail(email);
                    newUser.setName(name);
                    newUser.setProviderId(providerId);
                    newUser.setProvider("github");
                    newUser.setRole("USER");
                    return userRepository.save(newUser);
                });
    }
}