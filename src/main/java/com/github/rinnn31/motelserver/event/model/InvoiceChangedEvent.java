package com.github.rinnn31.motelserver.event.model;

public class InvoiceChangedEvent {
    public static record Created(
        String invoiceId
    ) {
    }

    public static record Paid(
        String invoiceId
    ) {
    }
}
