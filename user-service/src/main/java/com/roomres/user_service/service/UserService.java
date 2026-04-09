package com.roomres.user_service.service;

import com.roomres.user_service.model.User;
import com.roomres.user_service.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByProviderId(String providerId) {
        return userRepository.findByProviderId(providerId);
    }

    @Transactional
    public User processOAuthUser(String login, String email, String name, String providerId) {
        return userRepository.findByProviderId(providerId)
                .map(existingUser -> {
                    existingUser.setName(name);
                    existingUser.setEmail(email);
                    return userRepository.save(existingUser);
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
