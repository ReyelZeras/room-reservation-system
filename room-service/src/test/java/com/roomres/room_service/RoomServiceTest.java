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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
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
    @DisplayName("Deve retornar todas as salas")
    void findAll_Success() {
        when(roomRepository.findAll()).thenReturn(Arrays.asList(roomEntity));

        List<Room> result = roomService.findAll();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Sala Alpha", result.get(0).getName());
        verify(roomRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar uma sala por ID com sucesso")
    void findById_Success() {
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));

        Room result = roomService.findById(roomId);

        assertNotNull(result);
        assertEquals(roomId, result.getId());
        assertEquals("Sala Alpha", result.getName());
    }

    @Test
    @DisplayName("Deve retornar null ao buscar sala com ID inexistente (Tratamento Cache)")
    void findById_NotFound_ReturnsNull() {
        when(roomRepository.findById(any())).thenReturn(Optional.empty());

        Room result = roomService.findById(UUID.randomUUID());

        assertNull(result);
    }

    @Test
    @DisplayName("Deve salvar uma sala com sucesso")
    void save_Success() {
        when(roomRepository.save(any(Room.class))).thenReturn(roomEntity);

        Room result = roomService.save(roomEntity);

        assertNotNull(result);
        assertEquals(RoomStatus.AVAILABLE, result.getStatus());
        assertEquals("Sala Alpha", result.getName());
        verify(roomRepository, times(1)).save(any(Room.class));
    }

    @Test
    @DisplayName("Deve remover uma sala com sucesso")
    void deleteById_Success() {
        doNothing().when(roomRepository).deleteById(roomId);

        roomService.deleteById(roomId);

        verify(roomRepository, times(1)).deleteById(roomId);
    }
}