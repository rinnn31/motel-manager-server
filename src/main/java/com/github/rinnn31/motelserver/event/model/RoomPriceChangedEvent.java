package com.github.rinnn31.motelserver.event.model;

public record RoomPriceChangedEvent(
    String motelId,
    String roomId,
    int newPrice
) {
    
}
