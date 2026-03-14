package com.github.rinnn31.motelserver.dto.common;

import com.github.rinnn31.motelserver.entity.UserEntity;

public record UserInfo(
    String phoneNumber,
    String fullName,
    int gender
) {  
    public static UserInfo fromEntity(UserEntity user) {
        return new UserInfo(
            user.getPhoneNumber(),
            user.getFullName(),
            user.getGender()
        );
    }
}
