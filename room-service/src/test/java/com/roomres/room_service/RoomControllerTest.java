package com.roomres.room_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roomres.room_service.controller.RoomController;
import com.roomres.room_service.model.Room;
import com.roomres.room_service.model.RoomStatus;
import com.roomres.room_service.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoomController.class)
@AutoConfigureMockMvc(addFilters = false) // DESLIGA OS FILTROS DE SEGURANÇA (401/403) APENAS PARA ESTE TESTE
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoomService roomService;

    private Room roomEntity;
    private UUID roomId;

    @BeforeEach
    void setUp() {
        roomId = UUID.randomUUID();
        roomEntity = new Room();
        roomEntity.setId(roomId);
        roomEntity.setName("Sala Alpha");
        roomEntity.setCapacity(10);
        roomEntity.setLocation("Andar 1");
        roomEntity.setStatus(RoomStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Deve criar uma sala com sucesso retornando 201")
    void createRoom_ShouldReturnCreated() throws Exception {
        when(roomService.save(any(Room.class))).thenReturn(roomEntity);

        mockMvc.perform(post("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roomEntity)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Sala Alpha"));
    }

    @Test
    @DisplayName("Deve retornar lista de salas")
    void getAllRooms_ShouldReturnList() throws Exception {
        when(roomService.findAll()).thenReturn(Arrays.asList(roomEntity));

        mockMvc.perform(get("/api/v1/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Sala Alpha"));
    }

    @Test
    @DisplayName("Deve retornar uma sala especifica pelo ID")
    void getRoomById_ShouldReturnRoom() throws Exception {
        when(roomService.findById(roomId)).thenReturn(roomEntity);

        mockMvc.perform(get("/api/v1/rooms/" + roomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sala Alpha"));
    }

    @Test
    @DisplayName("Deve retornar 404 ao buscar sala inexistente")
    void getRoomById_NotFound_ShouldReturn404() throws Exception {
        when(roomService.findById(roomId)).thenReturn(null);

        mockMvc.perform(get("/api/v1/rooms/" + roomId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve atualizar uma sala com sucesso")
    void updateRoom_ShouldReturnUpdatedRoom() throws Exception {
        Room updatedRoom = new Room();
        updatedRoom.setName("Sala Beta");
        updatedRoom.setCapacity(20);
        updatedRoom.setLocation("Andar 2");
        updatedRoom.setStatus(RoomStatus.MAINTENANCE);

        when(roomService.findById(roomId)).thenReturn(roomEntity);
        when(roomService.save(any(Room.class))).thenReturn(updatedRoom);

        mockMvc.perform(put("/api/v1/rooms/" + roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedRoom)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sala Beta"));
    }

    @Test
    @DisplayName("Deve retornar 404 ao tentar atualizar sala inexistente")
    void updateRoom_NotFound_ShouldReturn404() throws Exception {
        when(roomService.findById(roomId)).thenReturn(null);

        mockMvc.perform(put("/api/v1/rooms/" + roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roomEntity)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve deletar uma sala com sucesso retornando 204")
    void deleteRoom_ShouldReturnSuccess() throws Exception {
        doNothing().when(roomService).deleteById(roomId);

        mockMvc.perform(delete("/api/v1/rooms/" + roomId))
                .andExpect(status().isNoContent());
    }
}