package com.github.rinnn31.motelserver.event.listener;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.github.rinnn31.motelserver.entity.NotificationType;
import com.github.rinnn31.motelserver.event.model.RoomMemberChangedEvent;
import com.github.rinnn31.motelserver.event.model.RoomNameChangedEvent;
import com.github.rinnn31.motelserver.event.model.RoomPriceChangedEvent;
import com.github.rinnn31.motelserver.repository.RoomMemberRepository;
import com.github.rinnn31.motelserver.service.NotificationService;
import com.github.rinnn31.motelserver.service.UserDeviceService;
import com.github.rinnn31.motelserver.service.external.PushNotificationService;

@Component
public class RoomEventListener extends AppEventListener {
    private final RoomMemberRepository roomMemberRepository;

    public RoomEventListener(
            NotificationService notificationService, 
            PushNotificationService pushNotificationService,
            UserDeviceService userDeviceService,
            RoomMemberRepository roomMemberRepository) {
        super(notificationService, pushNotificationService, userDeviceService);
        this.roomMemberRepository = roomMemberRepository;
    }

    @EventListener
    public void handleRoomNameChangedEvent(RoomNameChangedEvent event) {
        String title = "Tên phòng trọ của bạn đã được thay đổi";
        String body = "Chủ trọ của bạn đã thay đổi tên phòng trọ của bạn thành: " + event.newName();
        List<UUID> members = roomMemberRepository.findByRoom_IdAndEndDateIsNull(UUID.fromString(event.roomId())).stream()
            .map(rm -> rm.getUser().getId())
            .toList();

        sendNotification(
            members, 
            title, 
            body, 
            Map.of(
                "roomId", event.roomId(),
                "motelId", event.motelId()
            ),
            NotificationType.ROOM_INFO_CHANGED
        );
    }

    @EventListener
    public void handleRoomPriceChangedEvent(RoomPriceChangedEvent event) {
        String title = "Giá phòng trọ của bạn đã được cập nhật";
        String body = "Chủ trọ của bạn đã cập nhật giá phòng trọ của bạn thành: " + event.newPrice() + " VND";
        List<UUID> members = roomMemberRepository.findByRoom_IdAndEndDateIsNull(UUID.fromString(event.roomId())).stream()
            .map(rm -> rm.getUser().getId())
            .toList();

        sendNotification(
            members, 
            title, 
            body, 
            Map.of(
                "roomId", event.roomId(),
                "motelId", event.motelId()
            ),
            NotificationType.ROOM_INFO_CHANGED
        );
    }

    @EventListener
    public void handleRoomMemberAddedEvent(RoomMemberChangedEvent.Added event) {
        var ownerTitle = "Bạn đã được thêm vào một phòng trọ mới";
        var ownerBody = String.format("Chủ nhà trọ %s đã thêm bạn vào phòng trọ %s", event.motelName(), event.roomName());
        sendNotification(
            List.of(UUID.fromString(event.userId())), 
            ownerTitle, 
            ownerBody, 
            Map.of(
                "roomId", event.roomId(),
                "motelId", event.motelId()
            ),
            NotificationType.ROOM_MEMBER_CHANGED
        );

        var memberTitle = "Một thành viên mới đã được thêm vào phòng trọ của bạn";
        var memberBody = String.format("Chủ nhà trọ của bạn đã thêm thành viên %s vào phòng trọ của bạn", event.userName());
        List<UUID> members = roomMemberRepository.findByRoom_IdAndEndDateIsNull(UUID.fromString(event.roomId())).stream()
            .map(rm -> rm.getUser().getId())
            .filter(id -> !id.equals(UUID.fromString(event.userId())))
            .toList();
        sendNotification(
            members,
            memberTitle,
            memberBody,
            Map.of(
                "roomId", event.roomId(),
                "motelId", event.motelId(),
                "newMemberId", event.userId()
            ),
            NotificationType.ROOM_MEMBER_CHANGED
        );
    }

    @EventListener
    public void handleRoomMemberRemovedEvent(RoomMemberChangedEvent.Removed event) {
        var ownerTitle = "Bạn đã rời khỏi phòng trọ hiện tại";
        var ownerBody = String.format("Chủ nhà trọ %s đã xóa bạn khỏi phòng trọ %s", event.motelName(), event.roomName());
        sendNotification(
            List.of(UUID.fromString(event.userId())), 
            ownerTitle, 
            ownerBody,
            null,
            NotificationType.ROOM_MEMBER_CHANGED
        );

        var memberTitle = "Một thành viên đã rời khỏi phòng trọ của bạn";
        var memberBody = String.format("Chủ nhà trọ của bạn đã xóa thành viên %s khỏi phòng trọ của bạn", event.userName());
        List<UUID> members = roomMemberRepository.findByRoom_IdAndEndDateIsNull(UUID.fromString(event.roomId())).stream()
            .map(rm -> rm.getUser().getId())
            .filter(id -> !id.equals(UUID.fromString(event.userId())))
            .toList();
        sendNotification(
            members,
            memberTitle,
            memberBody,
            Map.of(
                "roomId", event.roomId(),
                "motelId", event.motelId()
            ),
            NotificationType.ROOM_MEMBER_CHANGED
        );
    }
}
