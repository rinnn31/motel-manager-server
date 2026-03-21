package com.github.rinnn31.motelserver.dto.account;

import jakarta.validation.constraints.NotBlank;

public record VerifyContactpointInput(
    @NotBlank(message = "validation.phone_number_required")
    String phoneNumber,
    @NotBlank(message = "validation.otp_required")
    String otp
) {
}
