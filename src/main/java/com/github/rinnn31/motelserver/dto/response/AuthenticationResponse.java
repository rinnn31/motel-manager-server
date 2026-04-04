package com.github.rinnn31.motelserver.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthenticationResponse(
    String accessToken,
    String refreshToken,
    String userId,
    Boolean verificationRequired,
    UserInfoResponse userInfo
) {
}
