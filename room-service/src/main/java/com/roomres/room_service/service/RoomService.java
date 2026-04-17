package com.roomres.room_service.service;

import com.roomres.room_service.model.Room;
import com.roomres.room_service.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository repository;

    // Quando este método for chamado, o Spring olha primeiro no Redis (chave "rooms")
    @Cacheable(value = "rooms", key = "'all-rooms'")
    public List<Room> findAll() {
        System.out.println("BUSCANDO NO BANCO DE DADOS (POSTGRES)...");
        return repository.findAll();
    }

    @Cacheable(value = "rooms", key = "#id")
    public Optional<Room> findById(UUID id) {
        return repository.findById(id);
    }

    // Sempre que salvar ou deletar, limpamos o cache para não servir dados velhos
    @CacheEvict(value = "rooms", allEntries = true)
    public Room save(Room room) {
        return repository.save(room);
    }

    @CacheEvict(value = "rooms", allEntries = true)
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}