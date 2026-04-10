package com.github.rinnn31.motelserver.web.advice;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.github.rinnn31.motelserver.dto.response.ApiResponse;
import com.github.rinnn31.motelserver.exception.AppError;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalErrorHandler {
    @ExceptionHandler(AppError.class)
    public ApiResponse<?> handleAppError(AppError ex) {
        String message = ex.getErrorCode().getMessage();
        return ApiResponse.error(ex.getErrorCode().name(), message, ex.getExtraData());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleGenericException(Exception ex) {
        System.err.println("Unhandled exception: " + ex.getMessage());
        return ApiResponse.error("INTERNAL_ERROR", "Lỗi máy chủ, vui lòng thử lại sau");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        var fieldError = ex.getFieldError();
        return ApiResponse.error("VALIDATION_ERROR", fieldError != null ? fieldError.getDefaultMessage() : "Dữ liệu không hợp lệ");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<?> handleConstraintViolationException(ConstraintViolationException ex) {
        var violation = ex.getConstraintViolations().iterator().next();
        return ApiResponse.error("VALIDATION_ERROR", violation.getMessage());
    }
}
