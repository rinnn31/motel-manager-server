package com.github.rinnn31.motelserver.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateMotelNameRequest(
    @NotBlank(message = "Tên nhà trọ không được để trống")
    @Size(max = 50, message = "Tên nhà trọ không được vượt quá 50 ký tự")
    String newName
) {
}
