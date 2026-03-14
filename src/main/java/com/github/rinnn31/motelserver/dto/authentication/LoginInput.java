package com.github.rinnn31.motelserver.dto.authentication;

import jakarta.validation.constraints.NotBlank;

public record LoginInput(
    @NotBlank(message = "validation.phone_not_blank")
    String phoneNumber, 

    @NotBlank(message = "validation.password_not_blank")
    String password) {
}
