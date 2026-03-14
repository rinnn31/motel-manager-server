package com.github.rinnn31.motelserver.exception;

public enum ErrorCode {
    /* Authentication */
    USER_NOT_FOUND("auth.user_not_found"),
    PHONE_NUMBER_USED("auth.user_already_exists"),
    USER_NOT_VERIFIED("auth.user_not_verified"),
    INVALID_CREDENTIALS("auth.invalid_credentials"),
    UNAUTHORIZED("auth.unauthorized"),
    VERIFY_FAILED("auth.verify_failed"),
    INVALID_REFRESH_TOKEN("auth.invalid_refresh_token"),

    /* Validation */
    PHONE_NUMBER_NOT_BLANK("validation.phone_not_blank"),
    PASSWORD_NOT_BLANK("validation.password_not_blank"),
    FULL_NAME_NOT_BLANK("validation.full_name_not_blank"),
    GENDER_NOT_BLANK("validation.gender_not_blank"),
    PASSWORD_TOO_SHORT("validation.password_too_short"),
    INVALID_GENDER("validation.invalid_gender"),
    VERIFICATION_CODE_NOT_BLANK("validation.verification_code_not_blank"),

    /* Otp */
    OTP_NOT_READY("otp.not_ready"),
    OTP_EXPIRED("otp.expired"),
    MAX_OTP_ATTEMPTS("otp.max_attempts"),
    OTP_SENDING_FAILED("otp.sending_failed"),

    /* General */
    INVALID_OPERATION("operation.invalid"), 
    ;
    private final String code;

    ErrorCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
