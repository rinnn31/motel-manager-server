package com.github.rinnn31.motelserver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties("sms.esms")
public record ESmsProperties(
    String apiKey,
    String apiSecret,
    String brandName
) {
    @ConstructorBinding
    public ESmsProperties {
    }
}