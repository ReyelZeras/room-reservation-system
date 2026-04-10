package com.roomres.room_service.controller;

import com.roomres.room_service.model.Room;
import com.roomres.room_service.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public ResponseEntity<List<Room>> getAllRooms() {
        return ResponseEntity.ok(roomService.findAll());
    }

    @PostMapping
    public ResponseEntity<Room> createRoom(@RequestBody Room room) {
        return ResponseEntity.status(201).body(roomService.save(room));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable UUID id) {
        // CORREÇÃO: Se não achar, retorna o status HTTP 404 (Not Found)
        return roomService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // NOVO: Atualizar uma sala existente
    @PutMapping("/{id}")
    public ResponseEntity<Room> updateRoom(@PathVariable UUID id, @RequestBody Room updatedRoom) {
        return roomService.findById(id).map(existing -> {
            existing.setName(updatedRoom.getName());
            existing.setCapacity(updatedRoom.getCapacity());
            existing.setLocation(updatedRoom.getLocation());
            existing.setStatus(updatedRoom.getStatus());
            return ResponseEntity.ok(roomService.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }
}