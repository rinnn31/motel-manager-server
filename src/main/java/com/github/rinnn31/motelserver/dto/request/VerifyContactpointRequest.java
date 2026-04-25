package com.github.rinnn31.motelserver.dto.request;

import com.github.rinnn31.motelserver.utils.validation.Phone;

import jakarta.validation.constraints.NotBlank;

public record VerifyContactpointRequest(
    @NotBlank(message = "Số điện thoại không được để trống")
    @Phone
    String phoneNumber,

    @NotBlank(message = "Mã OTP không được để trống")
    String otp
) {
}
