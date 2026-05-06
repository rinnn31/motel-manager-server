package com.github.rinnn31.motelserver.dto.response;

public record LandlordInfoResponse(
    String id,
    String fullName,
    String phoneNumber,
    Integer motelCount,
    Integer tenantCount
) {
}
