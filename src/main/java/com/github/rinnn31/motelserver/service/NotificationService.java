package com.github.rinnn31.motelserver.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.rinnn31.motelserver.dto.response.NotificationInfoResponse;
import com.github.rinnn31.motelserver.entity.NotificationType;
import com.github.rinnn31.motelserver.entity.User;
import com.github.rinnn31.motelserver.exception.AppError;
import com.github.rinnn31.motelserver.exception.ErrorCode;
import com.github.rinnn31.motelserver.repository.NotificationRepository;
import com.github.rinnn31.motelserver.security.Requester;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }
    
    public void createMessage(String title, String content, String extraData, NotificationType type, UUID targetUserId) {
        var user = new User();
        user.setId(targetUserId);
        var notification = new com.github.rinnn31.motelserver.entity.Notification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setExtraData(extraData);
        notification.setType(type);
        notification.setUser(user);
        notification.setCreatedAt(Instant.now());
        notificationRepository.save(notification);
    }

    public void markAsRead(UUID notificationId, Requester requester) {
        var notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            var notification = notificationOpt.get();
            if (!notification.getUser().getId().equals(requester.userId())) {
                throw new AppError(ErrorCode.INVALID_OPERATION);
            }

            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }

    public List<NotificationInfoResponse> getNotificationsForUser(Requester requester, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return notificationRepository.findByUser_Id(requester.userId(), pageable).stream()
        .map(n -> new NotificationInfoResponse(
            n.getId().toString(),
            n.getTitle(),
            n.getContent(),
            n.getExtraData(),
            n.getType().name(),
            n.isRead(),
            n.getCreatedAt().getEpochSecond()
        ))
        .toList();
    }

    @Transactional
    public void deleteNotification(UUID notificationId, Requester requester) {
        var notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            var notification = notificationOpt.get();
            if (!notification.getUser().getId().equals(requester.userId())) {
                throw new AppError(ErrorCode.INVALID_OPERATION);
            }

            notificationRepository.delete(notification);
        }
    }

    @Transactional
    public void deleteAllNotifications(Requester requester) {
        notificationRepository.deleteAllByUser_Id(requester.userId());
    }

    @Transactional
    public void markAllAsRead(Requester requester) {
        notificationRepository.markAllAsReadForUser(requester.userId());
    }  

}
