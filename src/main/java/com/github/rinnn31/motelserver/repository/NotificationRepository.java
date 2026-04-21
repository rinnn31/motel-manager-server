package com.github.rinnn31.motelserver.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.github.rinnn31.motelserver.entity.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByUser_IdOrderByCreatedAtDesc(UUID userId);

    List<Notification> findByUser_Id(UUID userId, Pageable pageable);

    void deleteAllByUser_Id(UUID userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId")
    void markAllAsReadForUser(UUID userId);
}
