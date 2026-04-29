package com.github.rinnn31.motelserver.controller.api;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.rinnn31.motelserver.dto.response.NotificationInfoResponse;
import com.github.rinnn31.motelserver.security.UserExtractor;
import com.github.rinnn31.motelserver.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationInfoResponse> getNotifications(@RequestParam (defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        return notificationService.getNotificationsForUser(requesterId, page, size);
    }

    @PatchMapping("/{notificationId}/read")
    public void markAsRead(@PathVariable UUID notificationId) {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        notificationService.markAsRead(notificationId, requesterId);
    }

    @PatchMapping("/read")
    public void markAllAsRead() {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        notificationService.markAllAsReadForUser(requesterId);
    }

    @DeleteMapping("/{notificationId}")
    public void deleteNotification(@PathVariable UUID notificationId) {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        notificationService.deleteNotification(notificationId, requesterId);
    }

    @DeleteMapping
    public void deleteAllNotifications() {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        notificationService.deleteAllNotificationsForUser(requesterId);
    }
}
