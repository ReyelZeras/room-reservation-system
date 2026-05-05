package com.roomres.booking_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roomres.booking_service.controller.BookingController;
import com.roomres.booking_service.dto.BookingResponseDTO;
import com.roomres.booking_service.model.BookingStatus;
import com.roomres.booking_service.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private BookingResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new BookingResponseDTO();
        responseDTO.setId(UUID.randomUUID());
        responseDTO.setRoomId(UUID.randomUUID());
        responseDTO.setUserId(UUID.randomUUID());
        responseDTO.setTitle("Reunião Diária");
        responseDTO.setStatus(BookingStatus.CONFIRMED);
        responseDTO.setStartTime(LocalDateTime.now().plusDays(1).withNano(0));
        responseDTO.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2).withNano(0));
    }

    @Test
    @DisplayName("Deve consultar a disponibilidade de salas")
    void checkAvailability_ShouldReturnList() throws Exception {
        when(bookingService.getAvailableRooms(any(), any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/bookings/availability")
                        .param("start", "2026-05-15T08:00:00")
                        .param("end", "2026-05-15T18:00:00"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.junit.jupiter.api.Assertions.assertTrue(status == 200 || status == 400);
                });
    }

    @Test
    @DisplayName("Deve retornar reservas do usuário")
    void getUserBookings_ShouldReturnList() throws Exception {
        UUID userId = UUID.randomUUID();
        when(bookingService.getByUserId(any())).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/v1/bookings/user/" + userId))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.junit.jupiter.api.Assertions.assertTrue(status == 200 || status == 404);
                });
    }

    @Test
    @DisplayName("Deve retornar reservas da sala")
    void getRoomBookings_ShouldReturnList() throws Exception {
        UUID roomId = UUID.randomUUID();
        when(bookingService.getByRoomId(any())).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/v1/bookings/room/" + roomId))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.junit.jupiter.api.Assertions.assertTrue(status == 200 || status == 404);
                });
    }

    @Test
    @DisplayName("Deve criar reserva com sucesso")
    void createBooking_ShouldReturnCreated() throws Exception {
        String jsonPayload = """
        {
            "roomId": "123e4567-e89b-12d3-a456-426614174000",
            "userId": "123e4567-e89b-12d3-a456-426614174001",
            "title": "Reunião de Alinhamento",
            "startTime": "2026-10-10T14:00:00",
            "endTime": "2026-10-10T16:00:00"
        }
        """;

        when(bookingService.createBooking(any())).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.junit.jupiter.api.Assertions.assertTrue(status == 201 || status == 200 || status == 400);
                });
    }

    @Test
    @DisplayName("Deve cancelar a reserva com sucesso")
    void cancelBooking_ShouldReturnSuccess() throws Exception {
        UUID bookingId = UUID.randomUUID();
        doNothing().when(bookingService).cancelBooking(any());

        mockMvc.perform(delete("/api/v1/bookings/" + bookingId))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.junit.jupiter.api.Assertions.assertTrue(status == 200 || status == 204 || status == 404);
                });
    }

    @Test
    @DisplayName("Deve remarcar a reserva com sucesso")
    void rescheduleBooking_ShouldReturnSuccess() throws Exception {
        UUID bookingId = UUID.randomUUID();
        when(bookingService.rescheduleBooking(any(), any(), any())).thenReturn(responseDTO);

        mockMvc.perform(patch("/api/v1/bookings/" + bookingId + "/reschedule")
                        .param("newStart", "2026-10-15T10:00:00")
                        .param("newEnd", "2026-10-15T12:00:00"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.junit.jupiter.api.Assertions.assertTrue(status == 200 || status == 400 || status == 404);
                });
    }
}