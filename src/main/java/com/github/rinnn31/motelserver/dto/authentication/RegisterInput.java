package com.github.rinnn31.motelserver.dto.authentication;

import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterInput(
    @NotBlank(message = "validation.phone_not_blank")
    String phoneNumber,

    @NotBlank(message = "validation.password_not_blank")
    @Size(min = 6, message = "validation.password_too_short")
    String password,

    @NotBlank(message = "validation.full_name_not_blank")
    String fullName,

    @NotNull(message = "validation.gender_not_blank")
    @Range(min = 0, max = 2, message = "validation.invalid_gender")
    Integer gender
) {
}
