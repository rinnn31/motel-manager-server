package com.github.rinnn31.motelserver.service.external.impl;

import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.github.rinnn31.motelserver.service.external.PushNotificationService;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;

@Component
@ConditionalOnProperty(name = "push-notification.provider", havingValue = "fcm")
public class FcmService implements PushNotificationService {
    @Override
    public List<String> sendNotification(List<String> deviceTokens, String title, String body, Map<String, String> extraData) {
        MulticastMessage message = MulticastMessage.builder()
            .addAllTokens(deviceTokens)
            .setNotification(Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build())
            .putAllData(extraData)
            .build();

        try {
            var response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            return response.getResponses().stream()
                .filter(r -> !r.isSuccessful())
                .map(r -> deviceTokens.get(response.getResponses().indexOf(r)))
                .toList();
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
            return List.of(); // Return empty list if sending fails
        }
    }
    
}
