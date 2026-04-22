package com.github.rinnn31.motelserver.dto.response;

import java.time.LocalDate;

public record MemberInfoResponse(
    UserInfoResponse user,
    String roomNumber,
    LocalDate joinDate
) {
    
}
