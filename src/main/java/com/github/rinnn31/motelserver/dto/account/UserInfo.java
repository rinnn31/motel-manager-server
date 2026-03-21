package com.github.rinnn31.motelserver.dto.account;

import com.github.rinnn31.motelserver.entity.User;

public record UserInfo(
    String phoneNumber,
    String fullName,
    int gender
) {  
    public static UserInfo fromEntity(User user) {
        return new UserInfo(
            user.getPhoneNumber(),
            user.getFullName(),
            user.getGender()
        );
    }
}
