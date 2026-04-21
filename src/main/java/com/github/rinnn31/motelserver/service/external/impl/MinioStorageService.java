package com.github.rinnn31.motelserver.service.external.impl;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.github.rinnn31.motelserver.config.properties.MinioProperties;
import com.github.rinnn31.motelserver.dto.response.MediaPresignedUrlResponse;
import com.github.rinnn31.motelserver.exception.AppError;
import com.github.rinnn31.motelserver.exception.ErrorCode;
import com.github.rinnn31.motelserver.service.external.ObjectStorageService;

import io.minio.MinioClient;
import io.minio.Http.Method;

@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "minio", matchIfMissing = true)
public class MinioStorageService implements ObjectStorageService {
    private final MinioProperties properties;

    private final MinioClient minioClient;

    public MinioStorageService(MinioClient minioClient, MinioProperties properties) {
        this.properties = properties;
        this.minioClient = minioClient;
    }

    @Override
    public MediaPresignedUrlResponse generatePresignedUrl(String contentType, String folder, int maxSizeInBytes) {
        try {
            String ext = contentType.split("/")[1];
            String key = folder + "/" + java.util.UUID.randomUUID() + "." + ext;
            String uploadUrl = minioClient.getPresignedObjectUrl(
                io.minio.GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket(properties.bucketName())
                    .object(key)
                    .expiry(10 * 60)
                    .extraQueryParams(Map.of("Content-Type", contentType))
                    .build()
            );
            return new MediaPresignedUrlResponse(uploadUrl, key);
        } catch (Exception e) {
            throw new AppError(ErrorCode.FILE_STORAGE_ERROR);
        }
    }

    @Override
    public String getPublicUrl(String key) {
        return properties.baseUrl() + "/" + properties.bucketName() + "/" + key;
    }

    @Override
    public void deleteFile(String key) {
        try {
            minioClient.removeObject(
                io.minio.RemoveObjectArgs.builder()
                    .bucket(properties.bucketName())
                    .object(key)
                    .build()
            );
        } catch (Exception e) {
            throw new AppError(ErrorCode.FILE_STORAGE_ERROR);
        }
    }

    @Override
    public boolean objectExists(String key) {
        try {
            minioClient.statObject(
                io.minio.StatObjectArgs.builder()
                    .bucket(properties.bucketName())
                    .object(key)
                    .build()
            );
            return true;
        } catch (io.minio.errors.ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                return false;
            }
            throw new AppError(ErrorCode.FILE_STORAGE_ERROR);
        } catch (Exception e) {
            throw new AppError(ErrorCode.FILE_STORAGE_ERROR);
        }
    }
}