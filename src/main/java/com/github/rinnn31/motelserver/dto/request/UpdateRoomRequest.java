package com.github.rinnn31.motelserver.dto.request;

import jakarta.validation.constraints.Positive;

public record UpdateRoomRequest(
    String roomNumber,
    
    @Positive(message = "Giá phòng phải là số dương")
    Integer roomPrice
) {
}
