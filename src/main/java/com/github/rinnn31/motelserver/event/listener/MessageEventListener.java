package com.github.rinnn31.motelserver.event.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.github.rinnn31.motelserver.event.model.MessageSentEvent;
import com.github.rinnn31.motelserver.repository.MemberRepository;
import com.github.rinnn31.motelserver.service.NotificationService;
import com.github.rinnn31.motelserver.service.UserDeviceService;
import com.github.rinnn31.motelserver.service.external.PushNotificationService;

@Component
public class MessageEventListener extends AppEventListener{
    private final MemberRepository roomMemberRepository;

    public MessageEventListener(
        NotificationService notificationService,
        PushNotificationService pushNotificationService, 
        UserDeviceService userDeviceService,
        MemberRepository roomMemberRepository
    ) {
        super(notificationService, pushNotificationService, userDeviceService);
        this.roomMemberRepository = roomMemberRepository;
    }
    
}
