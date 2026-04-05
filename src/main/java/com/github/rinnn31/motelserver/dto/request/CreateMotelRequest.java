package com.github.rinnn31.motelserver.dto.request;

import jakarta.validation.constraints.Size;

public record CreateMotelRequest(
    @Size(max = 50, message = "Tên nhà trọ không được vượt quá 50 ký tự")
    String displayName
) {
}
