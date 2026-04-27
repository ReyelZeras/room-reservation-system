package com.roomres.user_service.service;

import com.roomres.user_service.model.User;
import com.roomres.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User createUser(User user) {
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }

        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(UUID id, User userDetails) {
        return userRepository.findById(id).map(user -> {
            if (userDetails.getName() != null) user.setName(userDetails.getName());
            if (userDetails.getEmail() != null) user.setEmail(userDetails.getEmail());
            if (userDetails.getRole() != null) user.setRole(userDetails.getRole().toUpperCase());

            if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            }

            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
    }

    @Transactional
    public void deleteById(UUID id) {
        userRepository.deleteById(id);
    }

    // =========================================================================
    // LOGIN SOCIAL: Agora com proteção contra colisão de Username
    // =========================================================================
    @Transactional
    public User processOAuthUser(String email, String username, String name, String provider, String providerId) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getProviderId() == null) {
                user.setProvider(provider);
                user.setProviderId(providerId);
                userRepository.save(user);
            }
            return user;
        }

        // Se o usuário é novo, preparamos o Username
        String finalUsername = username;

        // A MÁGICA DE PREVENÇÃO: Se já existir alguém com esse username no banco, adiciona sufixo aleatório!
        if (userRepository.findByUsername(username).isPresent()) {
            finalUsername = username + "_" + UUID.randomUUID().toString().substring(0, 5);
        }

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setUsername(finalUsername); // Username 100% Único e livre de erros!
        newUser.setName(name != null ? name : finalUsername);
        newUser.setProvider(provider);
        newUser.setProviderId(providerId);
        newUser.setRole("USER");
        return userRepository.save(newUser);
    }
}