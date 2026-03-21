package com.github.rinnn31.motelserver.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties("sms.twilio")
public record TwilioProperties(
    String accountSid,
    String authToken,
    String fromNumber
) {
    @ConstructorBinding
    public TwilioProperties {
    }
}
