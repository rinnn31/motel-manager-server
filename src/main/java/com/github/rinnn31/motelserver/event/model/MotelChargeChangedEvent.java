package com.github.rinnn31.motelserver.event.model;

import com.github.rinnn31.motelserver.entity.ChargeType;

public class MotelChargeChangedEvent {
    public static record Created(
        String motelId,
        ChargeType chargeType,
        String description,
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
