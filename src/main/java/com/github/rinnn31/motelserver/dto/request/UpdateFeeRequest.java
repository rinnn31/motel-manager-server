package com.github.rinnn31.motelserver.dto.request;

import jakarta.validation.constraints.Positive;

public record UpdateFeeRequest(    
    String calculationType,

    @Positive(message = "Số tiền phải là số dương")
    int unitPrice
) {
    
}
