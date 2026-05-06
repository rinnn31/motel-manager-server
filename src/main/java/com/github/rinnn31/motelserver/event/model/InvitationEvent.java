package com.github.rinnn31.motelserver.event.model;

import java.util.UUID;

public class InvitationEvent {
    public static record InvaitationSent(
        String motelName,
        String inviterName,
        UUID inviteeId
    ) {
    }

    public static record InvitationAccepted(
        UUID inviterId,
        String userFullName,
        UUID motelId
    ) {
    }

    public static record InvitationDeclined(
        UUID inviterId,
        String userFullName,
        UUID motelId
    ) {
    }
}
