package com.github.rinnn31.motelserver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties("otp")
public record OtpProperties(
    int expirationMinutes,
    int maxAttemptsPerDay,
    int resendCooldownSeconds
) {
    @ConstructorBinding
    public OtpProperties {}
}
