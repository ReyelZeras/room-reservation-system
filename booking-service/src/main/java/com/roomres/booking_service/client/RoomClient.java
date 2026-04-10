package com.roomres.booking_service.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "room-service", url = "http://localhost:8082/api/v1/rooms")
public interface RoomClient {

    @GetMapping("/{id}")
    Object getRoomById(@PathVariable("id") UUID id);
    // Usamos Object apenas para verificar se retorna 200 ou 404
}