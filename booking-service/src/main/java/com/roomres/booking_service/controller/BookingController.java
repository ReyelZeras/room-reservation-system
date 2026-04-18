package com.roomres.booking_service.controller;

import com.roomres.booking_service.dto.BookingRequestDTO;
import com.roomres.booking_service.dto.BookingResponseDTO;
import com.roomres.booking_service.dto.RoomDTO;
import com.roomres.booking_service.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Endpoints para orquestração e gestão de reservas e disponibilidade.")
public class BookingController {

    private final BookingService bookingService;

    // NOVO: Endpoint vital para a UI desenhar o calendário
    @Operation(summary = "Pesquisar salas disponíveis", description = "Cruza dados do catálogo de salas com o histórico de reservas para devolver apenas as salas livres num determinado intervalo de tempo.")
    @GetMapping("/availability")
    public ResponseEntity<List<RoomDTO>> checkAvailability(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        return ResponseEntity.ok(bookingService.getAvailableRooms(start, end));
    }

    @Operation(summary = "Lista todas as reservas", description = "Retorna o histórico completo de reservas do sistema.")
    @GetMapping
    public ResponseEntity<List<BookingResponseDTO>> getAll() {
        return ResponseEntity.ok(bookingService.getAll());
    }

    @Operation(summary = "Busca reserva por ID", description = "Detalha uma reserva específica usando seu UUID.")
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.getById(id));
    }

    @Operation(summary = "Reservas de um usuário", description = "Lista todas as reservas atreladas a um usuário específico.")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponseDTO>> getByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(bookingService.getByUserId(userId));
    }

    @Operation(summary = "Agenda de uma sala", description = "Lista todas as reservas feitas para uma sala específica.")
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<BookingResponseDTO>> getByRoom(@PathVariable UUID roomId) {
        return ResponseEntity.ok(bookingService.getByRoomId(roomId));
    }

    @Operation(summary = "Criar nova reserva", description = "Verifica disponibilidade no Room Service, autentica no User Service, efetua a reserva e notifica o RabbitMQ.")
    @ApiResponse(responseCode = "201", description = "Reserva confirmada e notificação enviada")
    @ApiResponse(responseCode = "400", description = "Conflito de horários ou erro de serviço")
    @PostMapping
    public ResponseEntity<BookingResponseDTO> create(@RequestBody BookingRequestDTO dto) {
        return ResponseEntity.status(201).body(bookingService.createBooking(dto));
    }

    @Operation(summary = "Alterar horário da reserva", description = "Permite modificar o início ou fim de uma reserva existente, validando conflitos novamente.")
    @PatchMapping("/{id}/reschedule")
    public ResponseEntity<BookingResponseDTO> reschedule(
            @PathVariable UUID id,
            @RequestParam LocalDateTime newStart,
            @RequestParam LocalDateTime newEnd) {
        return ResponseEntity.ok(bookingService.rescheduleBooking(id, newStart, newEnd));
    }

    @Operation(summary = "Cancelar reserva", description = "Soft Delete: Altera o status da reserva para CANCELLED. Ela continuará no histórico.")
    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Map<String, String>> cancel(@PathVariable UUID id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.ok(Map.of("message", "Reserva cancelada com sucesso."));
    }

    @Operation(summary = "Excluir reserva", description = "Hard Delete: Remove permanentemente uma reserva do banco de dados.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePermanently(@PathVariable UUID id) {
        bookingService.deleteBookingPermanently(id);
        return ResponseEntity.noContent().build();
    }
}