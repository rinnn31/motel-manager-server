package com.github.rinnn31.motelserver.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = "admin")
public record AdminProperties(
    String username,
    String password
) {   
    @ConstructorBinding
    public AdminProperties {}
}
