package com.roomres.booking_service.client;

import com.roomres.booking_service.dto.RoomDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

// O Eureka resolve o nome "room-service" para o IP correto dinamicamente.
// Isto garante que não amarramos a nossa aplicação a um IP fixo de infraestrutura.
@FeignClient(name = "room-service")
public interface RoomClient {

    @GetMapping("/api/v1/rooms/{id}")
    Object getRoomById(@PathVariable("id") UUID id);

    // Novo endpoint para ir buscar todas as salas e cruzar com os nossos horários
    @GetMapping("/api/v1/rooms")
    List<RoomDTO> getAllRooms();
}