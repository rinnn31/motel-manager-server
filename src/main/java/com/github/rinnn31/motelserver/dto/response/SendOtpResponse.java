package com.github.rinnn31.motelserver.dto.response;

public record SendOtpResponse(
    boolean otpSent,
    int attemptsLeft,
    int remainingSeconds
) {
}
