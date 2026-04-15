package com.github.rinnn31.motelserver.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.github.rinnn31.motelserver.entity.Message;
import com.github.rinnn31.motelserver.entity.ObjectType;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findBySenderIdAndObjectTypeOrderByCreatedAtDesc(UUID senderId, ObjectType objectType);

    @Query("""
        SELECT DISTINCT m FROM Message m
        JOIN m.recipients r
        WHERE r.recipientId = :recipientId AND r.objectType = :objectType
        ORDER BY m.createdAt DESC
    """)
    List<Message> findByRecipientIdAndObjectTypeOrderByCreatedAtDesc(UUID recipientId, ObjectType objectType);
}
