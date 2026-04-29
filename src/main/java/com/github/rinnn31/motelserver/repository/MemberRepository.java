package com.github.rinnn31.motelserver.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.rinnn31.motelserver.entity.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, UUID> {
    List<Member> findByRoom_IdAndEndDateIsNull(UUID roomId);

    List<Member> findByRoom_IdInAndEndDateIsNull(List<UUID> roomIds);

    List<Member> findByRoom_Motel_IdAndEndDateIsNull(UUID motelId);

    int countByRoom_IdAndEndDateIsNull(UUID roomId);

    int countByRoom_Motel_IdAndEndDateIsNull(UUID motelId);

    boolean existsByRoom_IdAndEndDateIsNull(UUID roomId);

    boolean existsByRoom_Motel_IdAndEndDateIsNull(UUID motelId);

    boolean existsByUser_IdAndEndDateIsNull(UUID userId);

    boolean existsByUser_IdAndRoom_IdAndEndDateIsNull(UUID userId, UUID roomId);

    boolean existsByRoom_Motel_IdAndUser_IdAndEndDateIsNull(UUID motelId, UUID userId);

    Optional<Member> findByUser_IdAndRoom_IdAndEndDateIsNull(UUID userId, UUID roomId);

    Optional<Member> findByUser_IdAndEndDateIsNull(UUID userId);
}
