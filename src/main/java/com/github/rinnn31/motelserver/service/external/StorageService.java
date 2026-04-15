package com.github.rinnn31.motelserver.service.external;

public interface StorageService {
    String uploadFile(byte[] fileData, String filePath);

    void deleteFile(String fileUrl);
}
