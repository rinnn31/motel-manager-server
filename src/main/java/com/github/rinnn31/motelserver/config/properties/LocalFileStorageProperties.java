package com.github.rinnn31.motelserver.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("storage.local")
public record LocalFileStorageProperties(
    String baseDirectory
) {
    
}
