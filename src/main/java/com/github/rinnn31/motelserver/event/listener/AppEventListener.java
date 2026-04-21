package com.github.rinnn31.motelserver.event.listener;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.github.rinnn31.motelserver.entity.NotificationType;
import com.github.rinnn31.motelserver.service.NotificationService;
import com.github.rinnn31.motelserver.service.UserDeviceService;
import com.github.rinnn31.motelserver.service.external.PushNotificationService;
import com.github.rinnn31.motelserver.utils.JsonHelper;

public abstract class AppEventListener {
    protected final NotificationService notificationService;

    protected final PushNotificationService pushNotificationService;

    protected final UserDeviceService userDeviceService;

    public AppEventListener(NotificationService notificationService, PushNotificationService pushNotificationService, UserDeviceService userDeviceService) {
        this.notificationService = notificationService;
        this.pushNotificationService = pushNotificationService;
        this.userDeviceService = userDeviceService;
    }

    protected void sendNotification(List<UUID> recipientIds, String title, String body, Map<String, String> extraData, NotificationType type) {
        for (UUID recipientId : recipientIds) {
            notificationService.createMessage(title, body, JsonHelper.toJson(extraData), type, recipientId);
        }

        List<String> deviceTokens = recipientIds.stream()
            .flatMap(userId -> userDeviceService.getDeviceTokensForUser(userId).stream())
            .toList();

        if (!deviceTokens.isEmpty()) {
            var invalidTokens = pushNotificationService.sendNotification(deviceTokens, title, body, extraData);
            for (String token : invalidTokens) {
                userDeviceService.unregisterDeviceToken(token);
            }
        }
    }
}
