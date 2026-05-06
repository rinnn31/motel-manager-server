package com.github.rinnn31.motelserver.event.listener;

import org.springframework.context.event.EventListener;

import com.github.rinnn31.motelserver.event.model.InvitationEvent;
import com.github.rinnn31.motelserver.service.NotificationService;
import com.github.rinnn31.motelserver.service.UserDeviceService;
import com.github.rinnn31.motelserver.service.external.PushNotificationService;

public class InvitationEventListener extends AppEventListener {

    public InvitationEventListener(NotificationService notificationService,
            PushNotificationService pushNotificationService, UserDeviceService userDeviceService) {
        super(notificationService, pushNotificationService, userDeviceService);
    }

    @EventListener
    public void handleInvitationAcceptedEvent(InvitationEvent.InvitationAccepted event) {
        String title = "Lời mời của bạn đã được chấp nhận";
        String body = "Người dùng " + event.userFullName() + " đã chấp nhận lời mời tham gia nhà trọ của bạn.";
        sendNotification(
            java.util.List.of(event.inviterId()),
            title,
            body,
            java.util.Map.of("motelId", event.motelId().toString()),
            com.github.rinnn31.motelserver.entity.NotificationType.INVITATION
        );
    }

    @EventListener
    public void handleInvitationDeclinedEvent(InvitationEvent.InvitationDeclined event) {
        String title = "Lời mời của bạn đã bị từ chối";
        String body = "Người dùng " + event.userFullName() + " đã từ chối lời mời tham gia nhà trọ của bạn.";
        sendNotification(
            java.util.List.of(event.inviterId()),
            title,
            body,
            java.util.Map.of("motelId", event.motelId().toString()),
            com.github.rinnn31.motelserver.entity.NotificationType.INVITATION
        );
    }

    @EventListener
    public void handleInvitationSentEvent(InvitationEvent.InvaitationSent event) {
        String title = "Bạn nhận được một lời mời mới";
        String body = "Người dùng " + event.inviterName() + " đã mời bạn tham gia nhà trọ của họ.";
        sendNotification(
            java.util.List.of(event.inviteeId()),
            title,
            body,
            null,
            com.github.rinnn31.motelserver.entity.NotificationType.INVITATION
        );
    }
    
}
