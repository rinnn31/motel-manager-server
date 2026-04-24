package com.github.rinnn31.motelserver.service.external;

import com.github.rinnn31.motelserver.dto.response.MediaPresignedUrlResponse;
import com.github.rinnn31.motelserver.dto.response.ObjectMetadata;

public interface ObjectStorageService {
    MediaPresignedUrlResponse generatePresignedUrl(String contentType, String folder, int maxSizeInBytes);

    String getPublicUrl(String key);

    void deleteFile(String key);

    boolean objectExists(String key);

    ObjectMetadata statObject(String key);
}
