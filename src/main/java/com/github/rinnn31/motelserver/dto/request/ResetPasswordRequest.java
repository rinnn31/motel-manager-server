package com.github.rinnn31.motelserver.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
    @NotBlank(message = "Số điện thoại không được để trống")
    String phoneNumber,

    @NotBlank(message = "Mã xác minh không được để trống")
    String verificationCode,

    @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự")
    String newPassword
) {
}
