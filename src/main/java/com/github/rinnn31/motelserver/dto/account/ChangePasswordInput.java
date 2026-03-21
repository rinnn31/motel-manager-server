package com.github.rinnn31.motelserver.dto.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordInput(
    @NotBlank(message = "validation.old_password_required")
    String oldPassword,

    
    @Size(min = 6, message = "validation.password_too_short")
    @NotBlank(message = "validation.new_password_required")
    String newPassword
) {
}