package com.github.rinnn31.motelserver.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = "firebase")
public record FirebaseProperties(
    String serviceAccountKeyPath
) {
    @ConstructorBinding
    public FirebaseProperties {}
}