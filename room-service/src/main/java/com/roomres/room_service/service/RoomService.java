package com.roomres.room_service.service;

import com.roomres.room_service.model.Room;
import com.roomres.room_service.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    // Agora retorna Optional em vez de lançar RuntimeException direto
    public Optional<Room> findById(UUID id) {
        return roomRepository.findById(id);
    }

    @Transactional
    public Room save(Room room) {
        return roomRepository.save(room);
    }

    // ADICIONADO: Deleção de sala para fechar o CRUD
    @Transactional
    public void deleteById(UUID id) {
        roomRepository.deleteById(id);
    }
}