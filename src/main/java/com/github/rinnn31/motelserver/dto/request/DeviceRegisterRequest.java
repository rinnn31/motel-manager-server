package com.github.rinnn31.motelserver.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DeviceRegisterRequest(
    @NotBlank(message = "Mã thiết bị không được để trống")
    String deviceToken,

    @NotBlank(message = "Phiên đăng nhập không được để trống")
    String sessionToken
) {
}
