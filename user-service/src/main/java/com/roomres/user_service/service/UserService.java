package com.roomres.user_service.service;

import com.roomres.user_service.model.User;
import com.roomres.user_service.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Transactional
    public User processOAuthUser(String login, String email, String name, String providerId){
        return userRepository.findByProviderId(providerId)
                .map(existingUser ->{
                    //atualiza campos se necessario
                    existingUser.setName(name);
                    existingUser.setEmail(email);
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> {
                    //Cria novo utilizador
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

    public Optional<User> findByProviderId(String providerId){
        return userRepository.findByProviderId(providerId);
    }

}
