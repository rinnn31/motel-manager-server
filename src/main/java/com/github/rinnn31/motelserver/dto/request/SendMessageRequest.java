package com.github.rinnn31.motelserver.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

public record SendMessageRequest(
    @NotBlank(message = "Tiêu đề không được để trống")
    String title,
    
    String content,

    List<String> listEncodedImages
) {
}
