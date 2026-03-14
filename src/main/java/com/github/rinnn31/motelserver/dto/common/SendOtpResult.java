package com.github.rinnn31.motelserver.dto.common;

public record SendOtpResult(
    boolean otpSent,
    int attemptsLeft,
    int remainingSeconds
) {
}
