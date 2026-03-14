package com.github.rinnn31.motelserver.service.support;

public interface ISmsSender {
    boolean sendMessage(String phoneNumber, String message);
}
