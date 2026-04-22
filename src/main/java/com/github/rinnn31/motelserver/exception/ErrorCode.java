package com.github.rinnn31.motelserver.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    /* Common */
    INVALID_OPERATION("Hành động không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_ID("ID không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_DATE_RANGE("Khoảng thời gian không hợp lệ", HttpStatus.BAD_REQUEST),
    FILE_NOT_FOUND("Tệp tin không tồn tại", HttpStatus.NOT_FOUND),
    FILE_STORAGE_ERROR("Lỗi lưu trữ tệp", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_FILE_TYPE("Định dạng tệp không hợp lệ", HttpStatus.BAD_REQUEST),

    /* Authentication */
    PHONE_NUMBER_USED("Số điện thoại đã được sử dụng", HttpStatus.BAD_REQUEST),
    USER_NOT_VERIFIED("Tài khoản chưa được xác thực", HttpStatus.FORBIDDEN),
    INVALID_CREDENTIALS("Thông tin đăng nhập không hợp lệ", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("Không có quyền truy cập", HttpStatus.UNAUTHORIZED),
    VERIFY_FAILED("Xác thực thất bại", HttpStatus.BAD_REQUEST),
    INVALID_REFRESH_TOKEN("Refresh token không hợp lệ", HttpStatus.BAD_REQUEST),

    /* User */
    USER_NOT_FOUND("Người dùng không tồn tại", HttpStatus.NOT_FOUND),
    OLD_PASSWORD_INCORRECT("Mật khẩu cũ không đúng", HttpStatus.BAD_REQUEST),

    /* Otp */
    OTP_NOT_READY("Vui lòng chờ trước khi yêu cầu mã OTP mới", HttpStatus.TOO_MANY_REQUESTS),
    OTP_EXPIRED("Mã OTP đã hết hạn", HttpStatus.BAD_REQUEST),
    MAX_OTP_ATTEMPTS("Đã nhập sai mã OTP quá nhiều lần, vui lòng đợi trước khi thử lại", HttpStatus.TOO_MANY_REQUESTS),
    OTP_SENDING_FAILED("Gửi mã OTP thất bại, vui lòng thử lại sau", HttpStatus.INTERNAL_SERVER_ERROR),

    /* Motel */
    USER_NOT_LANDLORD("Người dùng không phải là chủ nhà trọ", HttpStatus.FORBIDDEN),
    MOTEL_NOT_FOUND("Nhà trọ không tồn tại", HttpStatus.NOT_FOUND), 
    ROOM_NOT_FOUND("Phòng không tồn tại", HttpStatus.NOT_FOUND),
    MOTEL_HAS_MEMBERS("Không thể xóa nhà trọ khi vẫn còn thành viên", HttpStatus.BAD_REQUEST),
    MOTEL_NAME_EXISTS("Tên nhà trọ đã tồn tại", HttpStatus.BAD_REQUEST),
    ROOM_NUMBER_EXISTS("Số phòng đã tồn tại trong nhà trọ này", HttpStatus.BAD_REQUEST),
    ROOM_HAS_MEMBERS("Không thể xóa phòng khi vẫn còn thành viên", HttpStatus.BAD_REQUEST), 
    INVOICE_NOT_FOUND("Hóa đơn không tồn tại", HttpStatus.NOT_FOUND),
    ROOM_NOT_SAME_MOTEL("Các phòng phải thuộc cùng một nhà trọ", HttpStatus.BAD_REQUEST), 
    MESSAGE_NOT_FOUND("Tin nhắn không tồn tại", HttpStatus.NOT_FOUND),
    ;
    private final String message;

    private final HttpStatus httpStatus;

    ErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}

