package com.github.rinnn31.motelserver.event.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.github.rinnn31.motelserver.event.model.MessageSentEvent;
import com.github.rinnn31.motelserver.repository.MemberRepository;
import com.github.rinnn31.motelserver.service.NotificationService;
import com.github.rinnn31.motelserver.service.UserDeviceService;
import com.github.rinnn31.motelserver.service.external.PushNotificationService;

@Component
public class MessageEventListener extends AppEventListener {
    private final MemberRepository roomMemberRepository;

    public MessageEventListener(
            NotificationService notificationService,
            PushNotificationService pushNotificationService,
            UserDeviceService userDeviceService,
            MemberRepository roomMemberRepository) {
        super(notificationService, pushNotificationService, userDeviceService);
        this.roomMemberRepository = roomMemberRepository;
    }

    @EventListener
    public void handleMessageSentFromMotel(MessageSentEvent.FromMotel event) {
        String title = "Bạn có một tin nhắn mới từ nhà trọ " + event.motelName();
        String body = event.messageTitle();
        var members = roomMemberRepository.findByRoom_IdInAndEndDateIsNull(
                event.roomIds().stream().map(id -> java.util.UUID.fromString(id)).toList()).stream()
                .map(rm -> rm.getUser().getId())
                .distinct()
                .toList();

        sendNotification(
                members,
                title,
                body,
                java.util.Map.of(
                        "motelId", event.motelId(),
                        "messageId", event.messageId()),
                com.github.rinnn31.motelserver.entity.NotificationType.MESSAGE);
    }

    @EventListener
    public void handleMessageSentFromRoom(MessageSentEvent.FromRoom event) {
        String title = "Bạn có một tin nhắn mới từ phòng trọ " + event.roomNumber() + " của nhà trọ " + event.motelName();
        String body = event.messageTitle();
        var members = roomMemberRepository.findByRoom_IdAndEndDateIsNull(java.util.UUID.fromString(event.roomId())).stream()
                .map(rm -> rm.getUser().getId())
                .toList();

        sendNotification(
                members,
                title,
                body,
                java.util.Map.of(
                        "motelId", event.motelId(),
                        "roomId", event.roomId(),
                        "messageId", event.messageId()),
                com.github.rinnn31.motelserver.entity.NotificationType.MESSAGE);
    }

}
