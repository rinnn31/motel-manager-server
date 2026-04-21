package com.github.rinnn31.motelserver.event.model;

public record RoomNameChangedEvent(
    String motelId,
    String roomId,
    String newName
) {
}
