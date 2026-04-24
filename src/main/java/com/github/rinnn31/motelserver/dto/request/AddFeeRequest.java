package com.github.rinnn31.motelserver.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AddFeeRequest(
    @Size(max = 20, message = "Tên phí không được vượt quá 20 ký tự")
    String name,

    @Positive(message = "Đơn giá phải là số dương")
    int unitPrice,

    @NotBlank(message = "Loại tính phí không được để trống")
    String calculationType
) {
    
}
