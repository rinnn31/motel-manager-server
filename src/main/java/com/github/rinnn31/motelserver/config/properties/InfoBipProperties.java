package com.github.rinnn31.motelserver.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = "sms.infobip")
public record InfoBipProperties(
    String apiKey,
    String baseUrl
) {
    @ConstructorBinding
    public InfoBipProperties {
    }
}
