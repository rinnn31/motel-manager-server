package com.github.rinnn31.motelserver.service.external.impl;

import java.net.URI;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.github.rinnn31.motelserver.config.properties.MinioProperties;
import com.github.rinnn31.motelserver.dto.response.MediaPresignedUrlResponse;
import com.github.rinnn31.motelserver.dto.response.ObjectMetadata;
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
                            .build());
            uploadUrl = replaceWithPublicEndpoint(uploadUrl);
            return new MediaPresignedUrlResponse(uploadUrl, key);
        } catch (Exception e) {
            throw new AppError(ErrorCode.FILE_STORAGE_ERROR);
        }
    }

    private String replaceWithPublicEndpoint(String url) {
        String publicEndpoint = properties.publicEndpoint();

        if (publicEndpoint == null || publicEndpoint.isBlank()) {
            return url;
        }

        URI uri = URI.create(url);

        return publicEndpoint.replaceAll("/+$", "")
                + uri.getRawPath()
                + (uri.getRawQuery() != null ? "?" + uri.getRawQuery() : "");
    }

    @Override
    public String getPublicUrl(String key) {
        if (key == null || !objectExists(key)) {
            throw new AppError(ErrorCode.FILE_NOT_FOUND);
        }

        String endpoint = properties.publicEndpoint().replaceAll("/+$", "");
        String bucket = properties.bucketName().replaceAll("^/+", "");
        key = key.replaceAll("^/+", "");

        return endpoint + "/" + bucket + "/" + key;
    }

    @Override
    public void deleteFile(String key) {
        try {
            minioClient.removeObject(
                    io.minio.RemoveObjectArgs.builder()
                            .bucket(properties.bucketName())
                            .object(key)
                            .build());
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
                            .build());
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

    @Override
    public ObjectMetadata statObject(String key) {
        try {
            var stat = minioClient.statObject(
                    io.minio.StatObjectArgs.builder()
                            .bucket(properties.bucketName())
                            .object(key)
                            .build());
            return new ObjectMetadata(
                    stat.size(),
                    stat.lastModified().toEpochSecond(),
                    stat.contentType(),
                    stat.etag(),
                    key);
        } catch (io.minio.errors.ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                throw new AppError(ErrorCode.FILE_NOT_FOUND);
            }
            throw new AppError(ErrorCode.FILE_STORAGE_ERROR);
        } catch (Exception e) {
            throw new AppError(ErrorCode.FILE_STORAGE_ERROR);
        }
    }
}