package com.github.rinnn31.motelserver.web.advice;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.github.rinnn31.motelserver.dto.response.ApiResponse;
import com.github.rinnn31.motelserver.exception.AppError;
import com.github.rinnn31.motelserver.exception.ErrorCode;

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
        ex.printStackTrace(); // Log lỗi để dễ dàng debug
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

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<ApiResponse<?>> handleMissingPathVariableException(MissingPathVariableException ex) {
        String errorMessage = String.format("Thiếu biến đường dẫn: %s", ex.getVariableName());
        return ResponseEntity.badRequest().body(ApiResponse.error("INVALID_REQUEST", errorMessage));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(405).body(ApiResponse.error("INVALID_REQUEST", "Phương thức không được hỗ trợ"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String errorMessage = String.format("Tham số '%s' có giá trị '%s' không hợp lệ", ex.getName(), ex.getValue());
        return ResponseEntity.badRequest().body(ApiResponse.error("VALIDATION_ERROR", errorMessage));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        return ResponseEntity.status(404).body(ApiResponse.error("NOT_FOUND", "Đường dẫn không tồn tại"));
    }

    // Create wrapper for invalid user in jwt filter
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        return ResponseEntity.status(401).body(ApiResponse.error(ErrorCode.UNAUTHORIZED.name(), "Người dùng không hợp lệ"));
    }
}
