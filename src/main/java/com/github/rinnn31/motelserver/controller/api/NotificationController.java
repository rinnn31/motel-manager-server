package com.github.rinnn31.motelserver.controller.api;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.rinnn31.motelserver.dto.response.NotificationInfoResponse;
import com.github.rinnn31.motelserver.security.Requester;
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
        Requester requester = Requester.fromContext();
        return notificationService.getNotificationsForUser(requester, page, size);
    }

    @PatchMapping("/{notificationId}/read")
    public void markAsRead(@PathVariable UUID notificationId) {
        Requester requester = Requester.fromContext();
        notificationService.markAsRead(notificationId, requester);
    }

    @PatchMapping("/read")
    public void markAllAsRead() {
        Requester requester = Requester.fromContext();
        notificationService.markAllAsRead(requester);
    }

    @DeleteMapping("/{notificationId}")
    public void deleteNotification(@PathVariable UUID notificationId) {
        Requester requester = Requester.fromContext();
        notificationService.deleteNotification(notificationId, requester);
    }

    @DeleteMapping
    public void deleteAllNotifications() {
        Requester requester = Requester.fromContext();
        notificationService.deleteAllNotifications(requester);
    }
}
