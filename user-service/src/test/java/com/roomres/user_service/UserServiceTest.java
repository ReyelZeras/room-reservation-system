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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserEventPublisher userEventPublisher;

    @InjectMocks
    private UserService userService;

    private User userEntity;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        userEntity = new User();
        userEntity.setId(userId);
        userEntity.setName("Administrador Sistema");
        userEntity.setEmail("admin@roomres.com");
        userEntity.setUsername("admin");
        userEntity.setPassword("senha123");
        userEntity.setRole("USER");
    }

    @Test
    @DisplayName("Deve retornar todos os usuarios")
    void findAll_Success() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(userEntity));

        List<User> result = userService.findAll();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Administrador Sistema", result.get(0).getName());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar um usuario por ID com sucesso")
    void findById_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        Optional<User> result = userService.findById(userId);

        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
        assertEquals("admin@roomres.com", result.get().getEmail());
    }

    @Test
    @DisplayName("Deve retornar empty ao buscar usuario com ID inexistente")
    void findById_NotFound_ReturnsEmpty() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        Optional<User> result = userService.findById(UUID.randomUUID());

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Deve salvar um usuario com sucesso")
    void createUser_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("senha_criptografada");
        when(userRepository.save(any(User.class))).thenReturn(userEntity);
        doNothing().when(userEventPublisher).sendVerificationEmailEvent(any(), any(), any());

        User result = userService.createUser(userEntity);

        assertNotNull(result);
        assertEquals("Administrador Sistema", result.getName());
        verify(userRepository, times(1)).save(any(User.class));
        verify(userEventPublisher, times(1)).sendVerificationEmailEvent(any(), any(), any());
    }

    @Test
    @DisplayName("Deve remover um usuario com sucesso")
    void deleteUser_Success() {
        doNothing().when(userRepository).deleteById(userId);

        userService.deleteById(userId);

        verify(userRepository, times(1)).deleteById(userId);
    }
}