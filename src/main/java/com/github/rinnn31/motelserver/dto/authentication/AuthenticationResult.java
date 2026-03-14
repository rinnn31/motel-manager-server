package com.github.rinnn31.motelserver.dto.authentication;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.rinnn31.motelserver.dto.common.UserInfo;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthenticationResult(
    String accessToken,
    String refreshToken,
    String userId,
    Boolean verificationRequired,
    UserInfo userInfo
) {
}
