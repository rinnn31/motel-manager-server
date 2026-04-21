package com.github.rinnn31.motelserver.event.model;

public class RoomMemberChangedEvent {
    public static record Added(
        String motelName,
        String motelId,
        String roomName,
        String roomId,
        String userId,
        String userName
    ) {
    }

    public static record Removed(
        String motelName,
        String motelId,
        String roomName,
        String roomId,
        String userId,
        String userName
    ) {
    }
}