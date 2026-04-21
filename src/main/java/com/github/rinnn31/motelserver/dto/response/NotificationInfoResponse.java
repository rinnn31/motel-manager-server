package com.github.rinnn31.motelserver.dto.response;

public record NotificationInfoResponse(
    String id,
    String title,
    String content,
    String extraData,
    String type
) {
}
