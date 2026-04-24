package com.github.rinnn31.motelserver.event.listener;

import java.util.Map;
import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.github.rinnn31.motelserver.entity.NotificationType;
import com.github.rinnn31.motelserver.event.model.MotelNameChangedEvent;
import com.github.rinnn31.motelserver.repository.MotelRepository;
import com.github.rinnn31.motelserver.repository.MemberRepository;
import com.github.rinnn31.motelserver.service.NotificationService;
import com.github.rinnn31.motelserver.service.UserDeviceService;
import com.github.rinnn31.motelserver.service.external.PushNotificationService;

@Component
public class MotelEventListener extends AppEventListener {

    private final MotelRepository motelRepository;

    private final MemberRepository roomMemberRepository;

    public MotelEventListener(
            NotificationService notificationService, 
            PushNotificationService pushNotificationService,
            UserDeviceService userDeviceService, 
            MotelRepository motelRepository,
            MemberRepository roomMemberRepository
    ) {
        super(notificationService, pushNotificationService, userDeviceService);
        this.motelRepository = motelRepository;
        this.roomMemberRepository = roomMemberRepository;
    }

    @EventListener
    public void handleMotelNameChangedEvent(MotelNameChangedEvent event) {
        String title = "Tên nhà trọ của bạn đã được thay đổi";
        String body = "Chủ trọ của bạn đã thay đổi tên nhà trọ của bạn thành: " + event.newName();
        var motelOpt = motelRepository.findById(UUID.fromString(event.motelId()));
        if (motelOpt.isEmpty()) {
            return;
        }
        var motel = motelOpt.get();
        var roomMembers = roomMemberRepository.findByRoom_Motel_IdAndEndDateIsNull(motel.getId());
        var memberIds = roomMembers.stream()
            .map(rm -> rm.getUser().getId())
            .distinct()
            .toList();

        sendNotification(
            memberIds, 
            title, 
            body, 
            Map.of("motelId", event.motelId()),
            NotificationType.MOTEL_INFO_CHANGED
        );
    }

    
}
