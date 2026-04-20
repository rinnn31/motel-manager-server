package com.github.rinnn31.motelserver.service.external;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "sms.provider", havingValue = "test")
public class TestSmsSender implements SmsSender {
    @Override
    public boolean sendMessage(String phoneNumber, String message) {
        System.out.println("Sending SMS to " + phoneNumber + ": " + message);
        return true; // Simulate successful sending
    }
    
}
