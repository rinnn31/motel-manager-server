package com.github.rinnn31.motelserver.dto.authentication;

import jakarta.validation.constraints.NotBlank;

public record LoginForm(
    @NotBlank(message = "Số điện thoại không được để trống")
    String phoneNumber, 

    @NotBlank(message = "Mật khẩu không được để trống")
    String password) {
}
