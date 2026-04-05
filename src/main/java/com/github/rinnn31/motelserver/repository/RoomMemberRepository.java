package com.github.rinnn31.motelserver.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.rinnn31.motelserver.entity.RoomMember;

@Repository
public interface RoomMemberRepository extends JpaRepository<RoomMember, UUID> {
    List<RoomMember> findByRoom_Id(UUID roomId);

    List<RoomMember> findByRoom_Motel_Id(UUID motelId);

    int countByRoom_IdAndEndDateIsNull(UUID roomId);

    int countByRoom_Motel_IdAndEndDateIsNull(UUID motelId);

    boolean existsByRoom_IdAndEndDateIsNull(UUID roomId);

    boolean existsByRoom_Motel_IdAndEndDateIsNull(UUID motelId);

    boolean existsByUser_IdAndEndDateIsNull(UUID userId);

    boolean existsByUser_IdAndRoom_IdAndEndDateIsNull(UUID userId, UUID roomId);

    Optional<RoomMember> findByUser_IdAndRoom_IdAndEndDateIsNull(UUID userId, UUID roomId);

    Optional<RoomMember> findByUser_IdAndEndDateIsNull(UUID userId);
}
