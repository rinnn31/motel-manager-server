package com.github.rinnn31.motelserver.dto.authentication;

public record UserIdTokenInput(
    String refreshToken,
    String userId
) {
}
