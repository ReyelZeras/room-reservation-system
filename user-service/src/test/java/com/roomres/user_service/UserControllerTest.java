package com.roomres.user_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roomres.user_service.controller.UserController;
import com.roomres.user_service.model.User;
import com.roomres.user_service.security.JwtService;
import com.roomres.user_service.service.CustomOAuth2UserService;
import com.roomres.user_service.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    // --- MOCKS INJETADOS PARA SATISFAZER O SPRING SECURITY ---
    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private AuthenticationProvider authenticationProvider;

    @MockBean
    private CustomOAuth2UserService customOAuth2UserService;
    // ---------------------------------------------------------

    private User userEntity;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        userEntity = new User();
        userEntity.setId(userId);
        userEntity.setName("Administrador Sistema");
        userEntity.setEmail("admin@roomres.com");
    }

    @Test
    @DisplayName("Deve criar um usuario com sucesso retornando 201")
    void createUser_ShouldReturnCreated() throws Exception {
        when(userService.createUser(any(User.class))).thenReturn(userEntity);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userEntity)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Administrador Sistema"))
                .andExpect(jsonPath("$.email").value("admin@roomres.com"));
    }

    @Test
    @DisplayName("Deve retornar lista de usuarios")
    void getAllUsers_ShouldReturnList() throws Exception {
        when(userService.findAll()).thenReturn(Arrays.asList(userEntity));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Administrador Sistema"));
    }

    @Test
    @DisplayName("Deve retornar um usuario especifico pelo ID")
    void getUserById_ShouldReturnUser() throws Exception {
        when(userService.findById(userId)).thenReturn(Optional.of(userEntity));

        mockMvc.perform(get("/api/v1/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Administrador Sistema"));
    }

    @Test
    @DisplayName("Deve retornar 404 ao buscar usuario inexistente")
    void getUserById_NotFound_ShouldReturn404() throws Exception {
        when(userService.findById(userId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/users/" + userId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve atualizar um usuario com sucesso")
    void updateUser_ShouldReturnUpdatedUser() throws Exception {
        User updatedUser = new User();
        updatedUser.setName("Usuario Atualizado");
        updatedUser.setEmail("atualizado@roomres.com");

        when(userService.updateUser(eq(userId), any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/v1/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Usuario Atualizado"));
    }

    @Test
    @DisplayName("Deve retornar 404 ao tentar atualizar usuario inexistente")
    void updateUser_NotFound_ShouldReturn404() throws Exception {
        when(userService.updateUser(eq(userId), any(User.class))).thenThrow(new RuntimeException("Usuário não encontrado."));

        mockMvc.perform(put("/api/v1/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userEntity)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve deletar um usuario com sucesso retornando 204")
    void deleteUser_ShouldReturnSuccess() throws Exception {
        doNothing().when(userService).deleteById(userId);

        mockMvc.perform(delete("/api/v1/users/" + userId))
                .andExpect(status().isNoContent());
    }
}