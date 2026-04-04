package com.github.rinnn31.motelserver.dto.response;

import com.github.rinnn31.motelserver.entity.User;

public record UserInfoResponse(
    String phoneNumber,
    String fullName,
    int gender
) {  
    public static UserInfoResponse fromEntity(User user) {
        return new UserInfoResponse(
            user.getPhoneNumber(),
            user.getFullName(),
            user.getGender()
        );
    }
}
