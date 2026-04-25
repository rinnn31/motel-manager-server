package com.github.rinnn31.motelserver.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
    @NotBlank(message = "Không được để trống refresh token")
    String refreshToken
) {
    
}
