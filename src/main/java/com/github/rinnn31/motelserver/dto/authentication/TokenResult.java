package com.github.rinnn31.motelserver.dto.authentication;

public record TokenResult(String accessToken, String refreshToken) {
}