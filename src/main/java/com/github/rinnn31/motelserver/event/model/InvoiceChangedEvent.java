package com.github.rinnn31.motelserver.event.model;

public class InvoiceChangedEvent {
    public static record Created(
        String invoiceId,
        String motelId,
        String roomId
    ) {
    }

    public static record Paid(
        String invoiceId,
        String motelId,
        String roomId
    ) {
    }

    public static record Delete(
        String motelId,
        String roomId
    ) {
    }
}
