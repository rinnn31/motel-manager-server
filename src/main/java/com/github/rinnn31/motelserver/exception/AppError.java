package com.github.rinnn31.motelserver.exception;

public class AppError extends RuntimeException {
    private final ErrorCode errorCode;
    private final Object extraData;

    public AppError(ErrorCode errorCode, Object extraData) {
        this.errorCode = errorCode;
        this.extraData = extraData;
    }

    public AppError(ErrorCode errorCode) {
        this(errorCode, null);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Object getExtraData() {
        return extraData;
    }
}
