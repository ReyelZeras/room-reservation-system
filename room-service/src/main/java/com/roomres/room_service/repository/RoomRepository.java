package com.roomres.room_service.repository;

import com.roomres.room_service.model.Room;
import com.roomres.room_service.model.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {
    List<Room> findByStatus(RoomStatus status);
}