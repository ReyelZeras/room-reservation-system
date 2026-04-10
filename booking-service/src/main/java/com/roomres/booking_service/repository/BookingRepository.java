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

    // NOVAS QUERIES PARA BUSCA ESPECÍFICA
    List<Booking> findByUserId(UUID userId);
    List<Booking> findByRoomId(UUID roomId);
}