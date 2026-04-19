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

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    // @Mock cria um banco de dados "falso" (dublê) para não tocarmos no Postgres real
    @Mock
    private UserRepository userRepository;

    // @InjectMocks injeta o banco de dados falso dentro do nosso UserService real
    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@roomres.com")
                .name("Test User")
                .role("USER")
                .build();
    }

    // Arquitetura: Testamos a camada de Service isolada da infraestrutura.
    // O padrão de teste segue o fluxo AAA (Arrange, Act, Assert) - Preparar, Agir, Validar.

    @Test
    @DisplayName("Deve retornar usuário por ID com sucesso")
    void shouldFindUserById() {
        // Arrange: Se alguém buscar por este ID, retorne nosso usuário fake
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // Act: Executa o método real
        Optional<User> result = userService.findById(user.getId());

        // Assert: Valida se o resultado é o esperado
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(userRepository, times(1)).findById(user.getId());
    }

    @Test
    @DisplayName("Deve salvar um novo usuário com sucesso")
    void shouldSaveUser() {
        when(userRepository.save(any(User.class))).thenReturn(user);

        User savedUser = userService.save(user);

        assertNotNull(savedUser);
        assertEquals("test@roomres.com", savedUser.getEmail());
        verify(userRepository, times(1)).save(user);
    }
}