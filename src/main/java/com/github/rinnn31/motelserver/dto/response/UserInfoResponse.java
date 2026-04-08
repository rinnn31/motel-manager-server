package com.github.rinnn31.motelserver.dto.response;

import com.github.rinnn31.motelserver.entity.User;

public record UserInfoResponse(
    String phoneNumber,
    String fullName,
    int gender,
    String role,
    boolean isVerified
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
