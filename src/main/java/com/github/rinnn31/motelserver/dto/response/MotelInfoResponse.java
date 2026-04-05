package com.github.rinnn31.motelserver.dto.response;

public record MotelInfoResponse(
    String id,
    String name,
    int memberCount
) {
}
