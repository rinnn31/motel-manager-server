package com.github.rinnn31.motelserver.service.external;

public interface ISmsSender {
    boolean sendMessage(String phoneNumber, String message);
}
