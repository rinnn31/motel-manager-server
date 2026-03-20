package com.github.rinnn31.motelserver.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties("security.jwt")
public record JwtProperties(
    String secret,
    long accessTokenTtl,
    long refreshTokenTtl
) {

    @ConstructorBinding
    public JwtProperties {
    }
}