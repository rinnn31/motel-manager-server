package com.github.rinnn31.motelserver.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserInfoResponse(
    String phoneNumber,
    String fullName,
    Integer gender,
    String role,
    Boolean isVerified,
    String avatarUrl
) {  
}
