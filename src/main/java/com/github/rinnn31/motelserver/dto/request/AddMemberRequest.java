package com.github.rinnn31.motelserver.dto.request;

import org.hibernate.validator.constraints.UUID;

import jakarta.validation.constraints.NotBlank;

public record AddMemberRequest(
    @NotBlank(message = "Không được để trống mã phòng")
    @UUID(message = "Mã phòng không hợp lệ")
    String roomId,

    @NotBlank(message = "Không được để trống mã người dùng")
    @UUID(message = "Mã người dùng không hợp lệ")
    String userId
) {
    
}
