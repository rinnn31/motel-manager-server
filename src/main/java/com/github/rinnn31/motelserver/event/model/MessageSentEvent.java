package com.github.rinnn31.motelserver.event.model;

import java.util.List;

public class MessageSentEvent {
    public static record FromMotel(
        String motelId,
        String motelName,
        List<String> roomIds,
        String messageId,
        String messageTitle
    ) {

    }

    public static record FromRoom(
        String motelId,
        String motelName,
        String roomId,
        String roomNumber,
        String messageId,
        String messageTitle
    ) {

    }
}
