package com.github.rinnn31.motelserver.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.rinnn31.motelserver.entity.MotelCharge;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChargeInfoResponse(
    String id,
    String chargeType,
    String description,
    String calculationType,
    int unitPrice
) {
    public ChargeInfoResponse(MotelCharge motelCharge) {
        this(
            motelCharge.getId().toString(),
            motelCharge.getType().name(),
            motelCharge.getDescription(),
            motelCharge.getCalculationType().name(),
            motelCharge.getUnitPrice()
        );
    }
}
