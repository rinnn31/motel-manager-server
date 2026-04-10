package com.github.rinnn31.motelserver.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.rinnn31.motelserver.entity.User;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserInfoResponse(
    String phoneNumber,
    String fullName,
    Integer gender,
    String role,
    Boolean isVerified
) {  
    public UserInfoResponse(User user) {
        this(
            user.getPhoneNumber(),
            user.getFullName(),
            user.getGender(),
            user.getRole().name(),
            user.isVerified()
        );
    }
}
