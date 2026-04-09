package com.github.rinnn31.motelserver.exception;

public enum ErrorCode {
    /* Common */
    INVALID_OPERATION("Hành động không hợp lệ"),
    INVALID_ID("ID không hợp lệ"),
    INVALID_DATE_RANGE("Khoảng thời gian không hợp lệ"), 

    /* Authentication */
    PHONE_NUMBER_USED("Số điện thoại đã được sử dụng"),
    USER_NOT_VERIFIED("Tài khoản chưa được xác thực"),
    INVALID_CREDENTIALS("Thông tin đăng nhập không hợp lệ"),
    UNAUTHORIZED("Không có quyền truy cập"),
    VERIFY_FAILED("Xác thực thất bại"),
    INVALID_REFRESH_TOKEN("Refresh token không hợp lệ"),

    /* User */
    USER_NOT_FOUND("Người dùng không tồn tại"),
    OLD_PASSWORD_INCORRECT("Mật khẩu cũ không đúng"),

    /* Otp */
    OTP_NOT_READY("Vui lòng chờ trước khi yêu cầu mã OTP mới"),
    OTP_EXPIRED("Mã OTP đã hết hạn"),
    MAX_OTP_ATTEMPTS("Đã nhập sai mã OTP quá nhiều lần, vui lòng đợi trước khi thử lại"),
    OTP_SENDING_FAILED("Gửi mã OTP thất bại, vui lòng thử lại sau"),

    /* Motel */
    USER_NOT_LANDLORD("Người dùng không phải là chủ nhà trọ"),
    MOTEL_NOT_FOUND("Nhà trọ không tồn tại"), 
    ROOM_NOT_FOUND("Phòng không tồn tại"),
    MOTEL_HAS_MEMBERS("Không thể xóa nhà trọ khi vẫn còn thành viên"),
    MOTEL_NAME_EXISTS("Tên nhà trọ đã tồn tại"),
    ROOM_NUMBER_EXISTS("Số phòng đã tồn tại trong nhà trọ này"),
    ROOM_HAS_MEMBERS("Không thể xóa phòng khi vẫn còn thành viên"), 
    INVOICE_NOT_FOUND("Hóa đơn không tồn tại"),
    ;
    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

