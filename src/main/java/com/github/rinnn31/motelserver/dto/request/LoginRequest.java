package com.github.rinnn31.motelserver.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "Số điện thoại không được để trống")
    String phoneNumber, 

    @NotBlank(message = "Mật khẩu không được để trống")
    String password) {
}
