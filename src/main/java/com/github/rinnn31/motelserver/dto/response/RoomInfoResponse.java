package com.github.rinnn31.motelserver.dto.response;

public record RoomInfoResponse(
    String id,
    String roomNumber,
    Integer price,
    Integer memberCount
) {
}
