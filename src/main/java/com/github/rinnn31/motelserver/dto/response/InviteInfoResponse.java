package com.github.rinnn31.motelserver.dto.response;

public record InviteInfoResponse(
    String inviteId, 
    String motelName,
    String roomNumber,
    String landlordName
) {
    
}
