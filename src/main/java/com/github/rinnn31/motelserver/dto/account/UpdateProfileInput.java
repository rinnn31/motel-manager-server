package com.github.rinnn31.motelserver.dto.account;

import org.hibernate.validator.constraints.Range;

public record UpdateProfileInput(
    String fullName,

    @Range(min = 0, max = 2, message = "validation.invalid_gender")
    Integer gender
) {
}
