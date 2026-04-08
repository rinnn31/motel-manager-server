package com.github.rinnn31.motelserver.service.external;

public interface SmsSender {
    boolean sendMessage(String phoneNumber, String message);
}
