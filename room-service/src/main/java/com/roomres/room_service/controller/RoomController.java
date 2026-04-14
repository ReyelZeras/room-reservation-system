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

    @Operation(summary = "Lista todas as salas", description = "Retorna uma lista com todas as salas cadastradas no sistema.")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    public ResponseEntity<List<Room>> getAllRooms() {
        return ResponseEntity.ok(roomService.findAll());
    }

    @Operation(summary = "Busca uma sala", description = "Encontra os detalhes de uma sala específica usando seu UUID.")
    @ApiResponse(responseCode = "200", description = "Sala encontrada")
    @ApiResponse(responseCode = "404", description = "Sala não encontrada")
    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable UUID id) {
        return roomService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Cadastra uma sala", description = "Cria uma nova sala no sistema e a disponibiliza para reservas.")
    @ApiResponse(responseCode = "201", description = "Sala criada com sucesso")
    @PostMapping
    public ResponseEntity<Room> createRoom(@RequestBody Room room) {
        return ResponseEntity.status(201).body(roomService.save(room));
    }

    @Operation(summary = "Atualiza uma sala", description = "Atualiza os dados de capacidade, localização ou status de uma sala.")
    @ApiResponse(responseCode = "200", description = "Sala atualizada com sucesso")
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

    @Operation(summary = "Excluir uma sala", description = "Remove permanentemente uma sala do catálogo. Cuidado: pode afetar o histórico de reservas.")
    @ApiResponse(responseCode = "204", description = "Sala removida com sucesso")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable UUID id) {
        roomService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}