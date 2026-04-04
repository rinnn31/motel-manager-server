package com.github.rinnn31.motelserver.dto.request;

import jakarta.validation.constraints.NotBlank;

public record VerifyContactpointRequest(
    @NotBlank(message = "Số điện thoại không được để trống")
    String phoneNumber,
    @NotBlank(message = "Mã OTP không được để trống")
    String otp
) {
}
