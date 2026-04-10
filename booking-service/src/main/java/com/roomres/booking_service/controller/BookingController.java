package com.roomres.booking_service.controller;

import com.roomres.booking_service.dto.BookingRequestDTO;
import com.roomres.booking_service.dto.BookingResponseDTO;
import com.roomres.booking_service.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    public ResponseEntity<List<BookingResponseDTO>> getAll() {
        return ResponseEntity.ok(bookingService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.getById(id));
    }

    // NOVOS ENDPOINTS MÚLTIPLOS
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponseDTO>> getByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(bookingService.getByUserId(userId));
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<BookingResponseDTO>> getByRoom(@PathVariable UUID roomId) {
        return ResponseEntity.ok(bookingService.getByRoomId(roomId));
    }

    @PostMapping
    public ResponseEntity<BookingResponseDTO> create(@RequestBody BookingRequestDTO dto) {
        return ResponseEntity.status(201).body(bookingService.createBooking(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> cancel(@PathVariable UUID id) {
        bookingService.cancelBooking(id);
        // CORREÇÃO: Agora retorna um JSON amigável e limpo com código 200 OK
        return ResponseEntity.ok(Map.of("message", "Reserva cancelada com sucesso."));
    }
}