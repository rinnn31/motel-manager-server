package com.github.rinnn31.motelserver.service.external;

import java.util.List;
import java.util.Map;

import com.github.rinnn31.motelserver.entity.NotificationType;

public interface PushNotificationService {
    // Return list of device tokens that failed to receive the notification
    List<String> sendNotification(List<String> deviceTokens, String title, String body, Map<String, String> extraData, NotificationType type);
}
