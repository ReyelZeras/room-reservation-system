package com.roomres.booking_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

// Removida a URL fixa.
@FeignClient(name = "room-service")
public interface RoomClient {
    @GetMapping("/api/v1/rooms/{id}")
    Object getRoomById(@PathVariable("id") UUID id);
}