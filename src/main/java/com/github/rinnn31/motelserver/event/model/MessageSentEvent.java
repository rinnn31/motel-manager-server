package com.github.rinnn31.motelserver.event.model;

public record MessageSentEvent(
    String motelId,
    String roomId,
    String messageId,
    String title,
    String receivedObjectType,
    // Extra fields for push notification, nullable
    String motelName,
    String roomNumber
) {
}
