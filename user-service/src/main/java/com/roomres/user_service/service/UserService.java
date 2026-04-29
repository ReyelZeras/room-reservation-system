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
        // PREVENÇÃO DE SEGURANÇA E DUAL WRITE
        // Verifica ANTES de fazer qualquer coisa no banco ou disparar e-mails
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new BusinessException("Este e-mail já está registrado no sistema.");
        }
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new BusinessException("Este nome de usuário já está em uso.");
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

        // Como validamos antes, é 100% seguro disparar o e-mail agora
        userEventPublisher.sendVerificationEmailEvent(savedUser.getEmail(), savedUser.getName(), savedUser.getVerificationToken());

        return savedUser;
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

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setUsername(finalUsername);
        newUser.setName(name != null ? name : finalUsername);
        newUser.setProvider(provider);
        newUser.setProviderId(providerId);
        newUser.setRole("USER");
        newUser.setActive(true); // O GitHub já verificou o email!
        return userRepository.save(newUser);
    }


    @Transactional
    public void requestPasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        // Por segurança (evitar enumeração de e-mails), não damos erro se não existir. Ignoramos silenciosamente.
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Usuários do GitHub não têm senha local para resetar.
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
            user.setResetPasswordToken(null); // Invalida o token após o uso
            userRepository.save(user);
            return true;
        }
        return false;
    }

    // NOVA FUNCIONALIDADE: Troca de Senha Logado
    @Transactional
    public void changeInternalPassword(UUID userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado."));

        if (!"local".equals(user.getProvider())) {
            throw new BusinessException("Contas vinculadas a serviços externos (como o GitHub) não podem alterar a senha por aqui.");
        }

        // Verifica se a senha antiga bate com o Hash do banco de dados
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BusinessException("A senha atual está incorreta.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}