package com.roomres.booking_service.dto;

import java.util.UUID;

// DTO para mapear as salas que chegam do room-service via OpenFeign.
// Optei por usar 'record' do Java 14+ pois é imutável, leve e reduz o boilerplate.
public record RoomDTO(
        UUID id,
        String name,
        Integer capacity,
        String location,
        String status
) {}