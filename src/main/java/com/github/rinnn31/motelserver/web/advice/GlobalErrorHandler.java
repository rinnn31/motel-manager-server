package com.github.rinnn31.motelserver.web.advice;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.github.rinnn31.motelserver.dto.common.ApiResponse;
import com.github.rinnn31.motelserver.exception.AppError;

@RestControllerAdvice
public class GlobalErrorHandler {
    @ExceptionHandler(AppError.class)
    public ApiResponse<?> handleAppError(AppError ex) {
        String translatedMessage = ex.getErrorCode().getCode();
        return ApiResponse.error(ex.getErrorCode().name(), translatedMessage, ex.getExtraData());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleGenericException(Exception ex) {
        System.err.println("Unhandled exception: " + ex.getMessage());
        return ApiResponse.error("internal_error", "An unexpected error occurred");
    }
}
