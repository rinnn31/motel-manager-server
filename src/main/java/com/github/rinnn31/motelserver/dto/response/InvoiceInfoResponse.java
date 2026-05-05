package com.github.rinnn31.motelserver.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record InvoiceInfoResponse(
    String id,
    Long createdAt,
    Long paidAt,
    String paymentStatus,
    List<InvoiceDetailsInfoResponse> details
) {
    public static record InvoiceDetailsInfoResponse(
        String name,
        int amount,
        int unitPrice,
        String calculationType
    ) {}
}
