package com.github.rinnn31.motelserver.dto.account;

public record SendOtpResult(
    boolean otpSent,
    int attemptsLeft,
    int remainingSeconds
) {
}
