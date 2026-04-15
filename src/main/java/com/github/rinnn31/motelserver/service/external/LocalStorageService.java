package com.github.rinnn31.motelserver.service.external;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.tika.Tika;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.github.rinnn31.motelserver.config.properties.LocalFileStorageProperties;
import com.github.rinnn31.motelserver.exception.AppError;
import com.github.rinnn31.motelserver.exception.ErrorCode;

@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    private final LocalFileStorageProperties properties;
    private final Tika tika = new Tika();

    public LocalStorageService(LocalFileStorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public String uploadFile(byte[] fileData, String filePath) {
        String mimeType = tika.detect(fileData);
        String extension = tika.detect(fileData).split("/")[1];
        if (!mimeType.startsWith("image/")) {
            throw new AppError(ErrorCode.INVALID_FILE_TYPE);
        }

        Path baseDir = Paths.get(properties.baseDirectory()).toAbsolutePath().normalize();
        Path targetPath = baseDir.resolve(filePath + "." + extension).normalize();

        try {
            Files.createDirectories(targetPath.getParent());
            Files.write(targetPath, fileData, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new AppError(ErrorCode.FILE_UPLOAD_FAILED);
        }

        return targetPath.toString();
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            Files.deleteIfExists(Paths.get(fileUrl));
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + fileUrl, e);
        }
    }
}