package com.github.rinnn31.motelserver.dto.authentication;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordInput(
    @NotBlank(message = "validation.phone_not_blank")
    String phoneNumber,

    @NotBlank(message = "validation.verification_code_not_blank")
    String verificationCode,

    @Size(min = 8, message = "validation.password_too_short")
    String newPassword
) {
}
