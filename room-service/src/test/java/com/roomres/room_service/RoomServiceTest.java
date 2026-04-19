package com.roomres.room_service;

import com.roomres.room_service.model.Room;
import com.roomres.room_service.model.RoomStatus;
import com.roomres.room_service.repository.RoomRepository;
import com.roomres.room_service.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomService roomService;

    private Room room;

    @BeforeEach
    void setUp() {
        room = Room.builder()
                .id(UUID.randomUUID())
                .name("Sala VIP")
                .capacity(10)
                .status(RoomStatus.AVAILABLE)
                .build();
    }

    @Test
    @DisplayName("Deve retornar lista de salas")
    void shouldReturnAllRooms() {
        when(roomRepository.findAll()).thenReturn(List.of(room));

        List<Room> rooms = roomService.findAll();

        assertFalse(rooms.isEmpty());
        assertEquals(1, rooms.size());
        assertEquals("Sala VIP", rooms.get(0).getName());
        verify(roomRepository, times(1)).findAll(); // Garante que o banco foi chamado 1 vez
    }

    @Test
    @DisplayName("Deve deletar sala por ID sem disparar erros")
    void shouldDeleteRoomById() {
        doNothing().when(roomRepository).deleteById(room.getId());

        roomService.deleteById(room.getId());

        verify(roomRepository, times(1)).deleteById(room.getId());
    }
}