package com.github.rinnn31.motelserver.event.model;

public class MotelChargeChangedEvent {
    public static record Created(
        String motelId,
        String name,
        double amount
    ) {
    }

    public static record Updated(
        String motelId
    ) {
    }

    public static record Deleted(
        String motelId
    ) {
    }
}
