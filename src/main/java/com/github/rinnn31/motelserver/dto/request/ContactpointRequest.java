package com.github.rinnn31.motelserver.dto.request;

import com.github.rinnn31.motelserver.utils.validation.Phone;

import jakarta.validation.constraints.NotBlank;

public record ContactpointRequest(
    @NotBlank(message = "Số điện thoại không được để trống")
    @Phone
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.github.rinnn31.motelserver.utils.PhoneE164Deserializer.class)
    String phoneNumber
) {
    
}
