package com.github.rinnn31.motelserver.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.github.rinnn31.motelserver.entity.Room;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {
    boolean existsByMotel_IdAndRoomNumber(UUID motelId, String roomNumber);

    List<Room> findByMotel_Id(UUID motelId);

    @Query("""
    SELECT COUNT(DISTINCT r.motel.id)
    FROM Room r
    WHERE r.id IN :roomIds
""")
    int countDistinctMotelByIdIn(@Param("roomIds") List<UUID> roomIds);

    List<Room> findByIdIn(List<UUID> roomIds);
}
