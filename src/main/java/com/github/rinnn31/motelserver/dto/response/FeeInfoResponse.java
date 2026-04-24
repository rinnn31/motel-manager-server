package com.github.rinnn31.motelserver.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.rinnn31.motelserver.entity.MotelFee;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FeeInfoResponse(
    String id,
    String name,
    String calculationType,
    int unitPrice
) {
    public FeeInfoResponse(MotelFee motelFee) {
        this(
            motelFee.getId().toString(),
            motelFee.getName(),
            motelFee.getCalculationType().name(),
            motelFee.getUnitPrice()
        );
    }
}
