package com.github.rinnn31.motelserver.dto.request;

import java.util.List;

import org.hibernate.validator.constraints.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(
    @Size(min = 1, message = "Phải có ít nhất một ID phòng")
    List<@UUID(message = "ID đối tượng không hợp lệ") String> targetRoomIds,

    @NotBlank(message = "Tiêu đề không được để trống")
    String title,

    @NotBlank(message = "Nội dung không được để trống")
    String content
) {
}
