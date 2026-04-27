package com.roomres.user_service;

import com.roomres.user_service.model.User;
import com.roomres.user_service.repository.UserRepository;
import com.roomres.user_service.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    // CORREÇÃO 1: Precisamos de fazer Mock do PasswordEncoder que adicionámos ao UserService
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@roomres.com")
                .password("senha123") // Adicionamos uma senha para o teste
                .name("Test User")
                .role("USER")
                .build();
    }

    @Test
    @DisplayName("Deve retornar usuário por ID com sucesso")
    void shouldFindUserById() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        Optional<User> result = userService.findById(user.getId());

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(userRepository, times(1)).findById(user.getId());
    }

    @Test
    @DisplayName("Deve criar um novo usuário com senha encriptada com sucesso")
    void shouldSaveUser() {
        // Ensinamos o nosso dublê de PasswordEncoder a devolver uma string qualquer quando for chamado
        when(passwordEncoder.encode(anyString())).thenReturn("senha_encriptada_mock");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // CORREÇÃO 2: Alterado de userService.save(user) para userService.createUser(user)
        User savedUser = userService.createUser(user);

        assertNotNull(savedUser);
        assertEquals("test@roomres.com", savedUser.getEmail());

        // Verificamos se o encriptador de senhas foi realmente invocado!
        verify(passwordEncoder, times(1)).encode("senha123");
        verify(userRepository, times(1)).save(user);
    }
}