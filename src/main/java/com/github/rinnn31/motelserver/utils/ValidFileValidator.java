package com.github.rinnn31.motelserver.utils;

import java.io.IOException;

import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidFileValidator implements ConstraintValidator<ValidFile, MultipartFile> {
    private final Tika tika = new Tika();

    private String[] allowedTypes;

    @Override
    public void initialize(ValidFile constraintAnnotation) {
        this.allowedTypes = constraintAnnotation.allowedTypes();
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            return true; // Không bắt buộc phải có hình ảnh
        }

        try {
            String contentType = tika.detect(file.getInputStream());
            for (String allowedType : allowedTypes) {
                if (allowedType.equalsIgnoreCase(contentType)) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }
    
}
