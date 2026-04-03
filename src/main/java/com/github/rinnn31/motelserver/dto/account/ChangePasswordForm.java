package com.github.rinnn31.motelserver.dto.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordForm(
    @NotBlank(message = "Mật khẩu cũ không được để trống")
    String oldPassword,

    
    @Size(min = 6, message = "Mật khẩu mới phải có ít nhất 6 ký tự")
    @NotBlank(message = "Mật khẩu mới không được để trống")
    String newPassword
) {
}