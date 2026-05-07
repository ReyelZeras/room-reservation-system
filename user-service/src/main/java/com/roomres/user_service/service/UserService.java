package com.roomres.user_service.service;

import com.roomres.user_service.exception.BusinessException;
import com.roomres.user_service.model.User;
import com.roomres.user_service.publisher.UserEventPublisher;
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
    private final UserEventPublisher userEventPublisher;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User createUser(User user) {
        // CORREÇÃO: IDEMPOTÊNCIA NO CADASTRO (Falha 7)
        Optional<User> existingUserByEmail = userRepository.findByEmail(user.getEmail());
        if (existingUserByEmail.isPresent()) {
            User found = existingUserByEmail.get();
            if (!found.isActive()) {
                // Usuário existe mas não ativou a conta. Reenviamos o email com novo token!
                found.setVerificationToken(UUID.randomUUID().toString());
                if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                    found.setPassword(passwordEncoder.encode(user.getPassword()));
                }
                found.setName(user.getName());
                User savedUser = userRepository.save(found);
                userEventPublisher.sendVerificationEmailEvent(savedUser.getEmail(), savedUser.getName(), savedUser.getVerificationToken());
                return savedUser;
            }
            throw new BusinessException("O e-mail informado já está em uso.");
        }

        Optional<User> existingUserByUsername = userRepository.findByUsername(user.getUsername());
        if (existingUserByUsername.isPresent()) {
            User found = existingUserByUsername.get();
            if (!found.isActive()) {
                found.setVerificationToken(UUID.randomUUID().toString());
                if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                    found.setPassword(passwordEncoder.encode(user.getPassword()));
                }
                found.setName(user.getName());
                User savedUser = userRepository.save(found);
                userEventPublisher.sendVerificationEmailEvent(savedUser.getEmail(), savedUser.getName(), savedUser.getVerificationToken());
                return savedUser;
            }
            throw new BusinessException("O username informado já está em uso.");
        }

        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        user.setActive(false);
        user.setVerificationToken(UUID.randomUUID().toString());

        User savedUser = userRepository.save(user);

        userEventPublisher.sendVerificationEmailEvent(savedUser.getEmail(), savedUser.getName(), savedUser.getVerificationToken());

        return savedUser;
    }

    @Transactional
    public User updateUser(UUID id, User userDetails) {
        return userRepository.findById(id).map(user -> {
            if (userDetails.getName() != null) user.setName(userDetails.getName());
            if (userDetails.getEmail() != null) user.setEmail(userDetails.getEmail());
            if (userDetails.getRole() != null) user.setRole(userDetails.getRole());
            return userRepository.save(user);
        }).orElseThrow(() -> new BusinessException("Usuário não encontrado com id " + id));
    }

    @Transactional
    public void deleteById(UUID id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public boolean verifyEmail(String token) {
        Optional<User> userOpt = userRepository.findByVerificationToken(token);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setActive(true);
            user.setVerificationToken(null);
            userRepository.save(user);
            return true;
        }
        return false;
    }

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

        String finalUsername = username;
        if (userRepository.findByUsername(username).isPresent()) {
            finalUsername = username + "_" + UUID.randomUUID().toString().substring(0, 5);
        }

        User newUser = User.builder()
                .email(email)
                .username(finalUsername)
                .name(name)
                .provider(provider)
                .providerId(providerId)
                .role("USER")
                .active(true)
                .build();
        return userRepository.save(newUser);
    }

    @Transactional
    public void requestPasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if ("local".equals(user.getProvider())) {
                user.setResetPasswordToken(UUID.randomUUID().toString());
                userRepository.save(user);
                userEventPublisher.sendPasswordResetEvent(user.getEmail(), user.getName(), user.getResetPasswordToken());
            }
        }
    }

    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<User> userOpt = userRepository.findByResetPasswordToken(token);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setResetPasswordToken(null);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Transactional
    public void changeInternalPassword(UUID userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado."));

        if (!"local".equals(user.getProvider())) {
            throw new BusinessException("Contas vinculadas a serviços externos (como o GitHub) não podem alterar a senha por aqui.");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BusinessException("A senha atual está incorreta.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public User toggleUserStatus(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado."));

        user.setActive(!user.isActive());
        return userRepository.save(user);
    }
}