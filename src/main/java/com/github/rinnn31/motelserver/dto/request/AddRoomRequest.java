package com.github.rinnn31.motelserver.dto.request;

import org.hibernate.validator.constraints.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record AddRoomRequest(
    @NotBlank(message = "Số phòng không được để trống")
    @Size(max = 10, message = "Số phòng không được vượt quá 10 ký tự")
    String roomNumber,

    @PositiveOrZero(message = "Giá phòng không được là số âm")
    int price,

    @NotBlank(message = "ID nhà trọ không được để trống")
    @UUID(message = "ID nhà trọ không hợp lệ")
    String motelId
) {
}
