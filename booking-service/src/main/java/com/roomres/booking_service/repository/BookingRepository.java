package com.roomres.booking_service.repository;

import com.roomres.booking_service.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Booking b WHERE b.roomId = :roomId AND b.status = 'CONFIRMED' AND (b.startTime < :endTime AND b.endTime > :startTime)")
    boolean existsConflictingBooking(
            @Param("roomId") UUID roomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // ADICIONADO: Valida conflitos ignorando a própria reserva (Essencial para o Update/Reschedule)
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Booking b WHERE b.roomId = :roomId AND b.status = 'CONFIRMED' AND b.id <> :bookingId AND (b.startTime < :endTime AND b.endTime > :startTime)")
    boolean existsConflictingBookingExcludingId(
            @Param("roomId") UUID roomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("bookingId") UUID bookingId
    );

    // QUERIES PARA BUSCA ESPECÍFICA
    List<Booking> findByUserId(UUID userId);
    List<Booking> findByRoomId(UUID roomId);


    // NOVO: Vai buscar todas as reservas 'CONFIRMADAS' que colidem com o intervalo de tempo especificado.
    // A lógica (startTime < limiteFim AND endTime > limiteInicio) é a matemática padrão para intersecção temporal.
    @Query("SELECT b FROM Booking b WHERE b.status = 'CONFIRMED' AND (b.startTime < :endTime AND b.endTime > :startTime)")
    List<Booking> findConflictingBookings(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}