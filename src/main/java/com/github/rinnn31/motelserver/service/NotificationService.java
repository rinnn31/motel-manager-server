package com.github.rinnn31.motelserver.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.github.rinnn31.motelserver.dto.response.NotificationInfoResponse;
import com.github.rinnn31.motelserver.entity.NotificationType;
import com.github.rinnn31.motelserver.entity.User;
import com.github.rinnn31.motelserver.exception.AppError;
import com.github.rinnn31.motelserver.exception.ErrorCode;
import com.github.rinnn31.motelserver.repository.NotificationRepository;

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
        notificationRepository.save(notification);
    }

    public void markAsRead(UUID notificationId, UUID requesterId) {
        var notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            var notification = notificationOpt.get();
            if (!notification.getUser().getId().equals(requesterId)) {
                throw new AppError(ErrorCode.INVALID_OPERATION);
            }

            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }

    public List<NotificationInfoResponse> getNotificationsForUser(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return notificationRepository.findByUser_Id(userId, pageable).stream()
            .map(n -> new NotificationInfoResponse(
                n.getId().toString(),
                n.getTitle(),
                n.getContent(),
                n.getExtraData(),
                n.getType().name()
            ))
            .toList();
    }

    public void deleteNotification(UUID notificationId, UUID requesterId) {
        var notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            var notification = notificationOpt.get();
            if (!notification.getUser().getId().equals(requesterId)) {
                throw new AppError(ErrorCode.INVALID_OPERATION);
            }

            notificationRepository.delete(notification);
        }
    }

    public void deleteAllNotificationsForUser(UUID userId) {
        notificationRepository.deleteAllByUser_Id(userId);
    }

    public void markAllAsReadForUser(UUID requesterId) {
        notificationRepository.markAllAsReadForUser(requesterId);
    }  

}
