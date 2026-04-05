package com.github.rinnn31.motelserver.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateChargeRequest(
    @Size(max = 20, message = "Mô tả không được vượt quá 20 ký tự")
    String description,
    
    String calculationType,

    @Positive(message = "Số tiền phải là số dương")
    int unitPrice
) {
    
}
