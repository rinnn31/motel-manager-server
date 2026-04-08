package com.github.rinnn31.motelserver.service.external;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.github.rinnn31.motelserver.config.properties.TwilioProperties;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import jakarta.annotation.PostConstruct;

@Service
@ConditionalOnProperty(name = "sms.provider", havingValue = "twilio")
public class TwilioSmsSender implements SmsSender {
    private final TwilioProperties properties;

    public TwilioSmsSender(TwilioProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        Twilio.init(properties.accountSid(), properties.authToken());
    }
    
    @Override
    public boolean sendMessage(String phoneNumber, String messageStr) {
        var message = Message.creator(
            new PhoneNumber(phoneNumber),
            new PhoneNumber(properties.fromNumber()),
            messageStr
        ).create();
        return message.getStatus() != Message.Status.FAILED;
    }
    
}
