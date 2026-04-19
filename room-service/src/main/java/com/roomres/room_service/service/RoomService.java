package com.roomres.room_service.service;

import com.roomres.room_service.model.Room;
import com.roomres.room_service.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository repository;

    // Arquitetura: Utilizo o Redis via @Cacheable. Como as características físicas de uma sala (nome, capacidade)
    // raramente mudam, evito idas constantes ao PostgreSQL. O tempo de vida (TTL) está configurado para 10 minutos.
    @Cacheable(value = "rooms", key = "'all-rooms'")
    public List<Room> findAll() {
        System.out.println("CACHE MISS: Acedendo à base de dados relacional...");
        return repository.findAll();
    }

    // Correção de Arquitetura: O Spring Cache lança erro 500 ao tentar salvar 'null' quando configurado
    // com disableCachingNullValues(). A instrução 'unless = "#result == null"' diz ao Spring
    // para simplesmente não fazer o cache caso o banco não encontre a sala, retornando o 404 limpo.
    @Cacheable(value = "rooms", key = "#id", unless = "#result == null")
    public Room findById(UUID id) {
        return repository.findById(id).orElse(null);
    }

    // Segurança de Dados: Ao modificar uma sala, é obrigatório invalidar o cache (@CacheEvict)
    // para impedir que o booking-service receba "dados fantasma" e crie reservas em salas desativadas.
    @CacheEvict(value = "rooms", allEntries = true)
    public Room save(Room room) {
        return repository.save(room);
    }

    @CacheEvict(value = "rooms", allEntries = true)
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}