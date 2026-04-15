package com.github.rinnn31.motelserver.dto.response;

import java.time.LocalDate;

public record RoomMemberResponse(
    String id,
    String fullName,
    String phoneNumber,
    LocalDate startDate
) {
    
}
