package com.github.rinnn31.motelserver.dto.account;

import org.hibernate.validator.constraints.Range;

public record UpdateProfileForm(
    String fullName,

    @Range(min = 0, max = 2, message = "Giới tính không hợp lệ")
    Integer gender
) {
}
