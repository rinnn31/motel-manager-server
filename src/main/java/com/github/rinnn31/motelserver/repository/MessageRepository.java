package com.github.rinnn31.motelserver.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.github.rinnn31.motelserver.entity.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findByMotelSender_IdOrderByCreatedAtDesc(UUID motelSenderId);

    List<Message> findByRoomSender_IdOrderByCreatedAtDesc(UUID roomSenderId);

    @Query("""
        SELECT DISTINCT m FROM Message m
        JOIN m.recipients r
        WHERE r.motelRecipient.id = :recipientId
        ORDER BY m.createdAt DESC
    """)
    List<Message> findByMotelRecipient_Id(UUID recipientId);

    @Query("""
        SELECT DISTINCT m FROM Message m
        JOIN m.recipients r
        WHERE r.roomRecipient.id = :recipientId
        ORDER BY m.createdAt DESC
    """)
    List<Message> findByRoomRecipient_Id(UUID recipientId);
}
