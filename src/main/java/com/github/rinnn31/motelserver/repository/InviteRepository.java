package com.github.rinnn31.motelserver.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import com.github.rinnn31.motelserver.entity.Invite;
import org.springframework.stereotype.Repository;

@Repository
public interface InviteRepository extends JpaRepository<Invite, UUID> {
    List<Invite> findByUser_IdAndExpiredAtAfter(UUID userId, LocalDateTime now);

    boolean existsByRoom_Motel_IdAndUser_IdAndExpiredAtAfter(UUID motelId, UUID userId, LocalDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void deleteAllByUser_Id(UUID userId);
}
