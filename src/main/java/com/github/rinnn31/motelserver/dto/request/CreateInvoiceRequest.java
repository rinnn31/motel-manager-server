package com.github.rinnn31.motelserver.dto.request;

import java.util.List;

import org.hibernate.validator.constraints.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateInvoiceRequest(
    @NotBlank(message = "ID phòng không được để trống")
    @UUID(message = "ID phòng không hợp lệ")
    String roomId,

    @NotEmpty(message = "Danh sách chi tiết hóa đơn không được để trống")
    List<CreateInvoiceDetailsRequest> details
) {
    public static record CreateInvoiceDetailsRequest(
        @NotBlank(message = "Tên khoản thu không được để trống")
        String name,

        @Positive(message = "Số lượng phải là số dương")
        @NotNull(message = "Số lượng không được để trống")
        Integer amount,

        @Positive(message = "Đơn giá phải là số dương")
        @NotNull(message = "Đơn giá không được để trống")
        Integer unitPrice,

        @NotBlank(message = "Loại tính toán không được để trống")
        String calculationType
    ) {}
}
