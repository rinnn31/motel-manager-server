package com.github.rinnn31.motelserver.event.model;

public record MotelNameChangedEvent(
    String motelId,
    String newName
) {    
}