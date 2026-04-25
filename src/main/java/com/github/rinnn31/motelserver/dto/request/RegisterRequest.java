package com.github.rinnn31.motelserver.dto.request;

import org.hibernate.validator.constraints.Range;

import com.github.rinnn31.motelserver.utils.validation.Phone;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "Số điện thoại không được để trống")
    @Phone
    String phoneNumber,

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    String password,

    @NotBlank(message = "Họ và tên không được để trống")
    String fullName,

    @NotNull(message = "Giới tính không được để trống")
    @Range(min = 0, max = 2, message = "Giới tính không hợp lệ")
    Integer gender,

    @NotNull(message = "Vai trò không được để trống")
    @Range(min = 0, max = 1, message = "Vai trò không hợp lệ")
    Integer role
) {
}
