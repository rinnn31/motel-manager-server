package com.github.rinnn31.motelserver.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.rinnn31.motelserver.config.properties.MinioProperties;

import io.minio.MinioClient;

@Configuration
public class MinioConfig {
    @Bean
    public MinioClient minioClient(MinioProperties properties) {
        return MinioClient.builder()
            .endpoint(properties.endpoint())
            .credentials(properties.accessKey(), properties.secretKey())
            .build();
    }

    @Bean
    public CommandLineRunner initMinioBucket(MinioClient minioClient, MinioProperties properties) {
        return args -> {
            boolean bucketExists = minioClient.bucketExists(
                io.minio.BucketExistsArgs.builder()
                    .bucket(properties.bucketName())
                    .build()
            );
            if (!bucketExists) {
                minioClient.makeBucket(
                    io.minio.MakeBucketArgs.builder()
                        .bucket(properties.bucketName())
                        .build()
                );
            }

            // Make bucket public
            minioClient.setBucketPolicy(
                io.minio.SetBucketPolicyArgs.builder()
                    .bucket(properties.bucketName())
                    .config("{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":\"*\",\"Action\":[\"s3:GetObject\"],\"Resource\":[\"arn:aws:s3:::" + properties.bucketName() + "/*\"]}]}")
                    .build()
            );
        };
    }
}
