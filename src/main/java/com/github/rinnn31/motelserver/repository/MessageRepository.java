package com.github.rinnn31.motelserver.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.github.rinnn31.motelserver.entity.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findByMotelSender_IdAndCreatedAtBetweenOrderByCreatedAtDesc(UUID motelSenderId, Instant from, Instant to, Pageable pageable);

    List<Message> findByRoomSender_IdAndCreatedAtBetweenOrderByCreatedAtDesc(UUID roomSenderId, Instant from, Instant to, Pageable pageable);

    @Query("""
        SELECT DISTINCT m FROM Message m
        JOIN m.recipients r
        WHERE r.motelRecipient.id = :recipientId AND m.createdAt >= :from AND m.createdAt <= :to
        ORDER BY m.createdAt DESC
    """)
    List<Message> findByMotelRecipient_Id(UUID recipientId, Instant from, Instant to, Pageable pageable);

    @Query("""
        SELECT DISTINCT m FROM Message m
        JOIN m.recipients r
        WHERE r.roomRecipient.id = :recipientId AND m.createdAt >= :from AND m.createdAt <= :to
        ORDER BY m.createdAt DESC
    """)
    List<Message> findByRoomRecipient_Id(UUID recipientId, Instant from, Instant to, Pageable pageable);
}
