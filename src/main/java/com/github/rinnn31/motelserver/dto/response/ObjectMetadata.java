package com.github.rinnn31.motelserver.dto.response;

public record ObjectMetadata(
    long size,
    long lastModified,
    String contentType,
    String eTag,
    String key
) {
    
}
