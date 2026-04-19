package com.roomres.room_service.controller;

import com.roomres.room_service.model.Room;
import com.roomres.room_service.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
@Tag(name = "Rooms", description = "Endpoints de gerenciamento de salas")
public class RoomController {

    private final RoomService roomService;

    @Operation(summary = "Lista todas as salas")
    @GetMapping
    public ResponseEntity<List<Room>> getAllRooms() {
        return ResponseEntity.ok(roomService.findAll());
    }

    @Operation(summary = "Busca uma sala")
    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable UUID id) {
        Room room = roomService.findById(id);
        return room != null ? ResponseEntity.ok(room) : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Cadastra uma sala")
    @PostMapping
    public ResponseEntity<Room> createRoom(@RequestBody Room room) {
        return ResponseEntity.status(201).body(roomService.save(room));
    }

    @Operation(summary = "Atualiza uma sala")
    @PutMapping("/{id}")
    public ResponseEntity<Room> updateRoom(@PathVariable UUID id, @RequestBody Room updatedRoom) {
        Room existing = roomService.findById(id);

        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        existing.setName(updatedRoom.getName());
        existing.setCapacity(updatedRoom.getCapacity());
        existing.setLocation(updatedRoom.getLocation());
        existing.setStatus(updatedRoom.getStatus());

        return ResponseEntity.ok(roomService.save(existing));
    }

    @Operation(summary = "Excluir uma sala")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable UUID id) {
        roomService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}