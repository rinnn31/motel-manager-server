package com.github.rinnn31.motelserver.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = "storage.minio")
public record MinioProperties(
    String endpoint,
    String accessKey,
    String secretKey,
    String bucketName,
    String baseUrl
) {
    @ConstructorBinding
    public MinioProperties {
    }
}
