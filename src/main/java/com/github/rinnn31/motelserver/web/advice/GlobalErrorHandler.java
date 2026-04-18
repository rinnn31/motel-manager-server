package com.github.rinnn31.motelserver.web.advice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.github.rinnn31.motelserver.dto.response.ApiResponse;
import com.github.rinnn31.motelserver.exception.AppError;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalErrorHandler {
    @ExceptionHandler(AppError.class)
    public ResponseEntity<ApiResponse<?>> handleAppError(AppError ex) {
        return ResponseEntity.status(ex.getErrorCode().getHttpStatus())
            .body(ApiResponse.error(ex.getErrorCode().name(), ex.getErrorCode().getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGenericException(Exception ex) {
        System.err.println("Unhandled exception: " + ex.getMessage());
        return ResponseEntity.status(500).body(ApiResponse.error("INTERNAL_SERVER_ERROR", "Đã có lỗi xảy ra, vui lòng thử lại sau"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        var fieldError = ex.getFieldError();
        String errorMessage = fieldError != null ? fieldError.getDefaultMessage() : "Dữ liệu không hợp lệ";
        return ResponseEntity.badRequest().body(ApiResponse.error("VALIDATION_ERROR", errorMessage));    
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleConstraintViolationException(ConstraintViolationException ex) {
        var violation = ex.getConstraintViolations().iterator().next();
        return ResponseEntity.badRequest().body(ApiResponse.error("VALIDATION_ERROR", violation.getMessage()));
    }
}
