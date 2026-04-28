package com.roomres.user_service;

import com.roomres.user_service.model.User;
import com.roomres.user_service.publisher.UserEventPublisher;
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

    @Mock
    private PasswordEncoder passwordEncoder;

    // A MÁGICA QUE FALTAVA: O Mock do mensageiro do RabbitMQ!
    @Mock
    private UserEventPublisher userEventPublisher;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@roomres.com")
                .password("senha123")
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
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("senha_encriptada_mock");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Dizemos ao Mock para não fazer nada quando pedirmos para enviar o e-mail (pois é só um teste)
        doNothing().when(userEventPublisher).sendVerificationEmailEvent(anyString(), anyString(), anyString());

        User savedUser = userService.createUser(user);

        assertNotNull(savedUser);
        assertEquals("test@roomres.com", savedUser.getEmail());

        verify(passwordEncoder, times(1)).encode("senha123");
        verify(userRepository, times(1)).save(user);
        // Verifica se o método de disparo de e-mail foi chamado!
        verify(userEventPublisher, times(1)).sendVerificationEmailEvent(anyString(), anyString(), anyString());
    }
}