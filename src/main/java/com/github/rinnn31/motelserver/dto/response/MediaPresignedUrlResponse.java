package com.github.rinnn31.motelserver.dto.response;

public record MediaPresignedUrlResponse(
    String uploadUrl,
    String key
) {
}
