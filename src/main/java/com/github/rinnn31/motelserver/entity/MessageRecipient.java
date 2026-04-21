package com.github.rinnn31.motelserver.entity;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "message_recipients",
    indexes = {
        @Index(name = "idx_motel_recipient_id", columnList = "motel_recipient_id"),
        @Index(name = "idx_room_recipient_id", columnList = "room_recipient_id")
    }
)
public class MessageRecipient {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "message_id", nullable = false, referencedColumnName = "id")
    private Message message;

    @JoinColumn(name = "motel_recipient_id", referencedColumnName = "id")
    private Motel motelRecipient;

    @JoinColumn(name = "room_recipient_id", referencedColumnName = "id")
    private Room roomRecipient;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Motel getMotelRecipient() {
        return motelRecipient;
    }

    public void setMotelRecipient(Motel motelRecipient) {
        this.motelRecipient = motelRecipient;
    }

    public Room getRoomRecipient() {
        return roomRecipient;
    }

    public void setRoomRecipient(Room roomRecipient) {
        this.roomRecipient = roomRecipient;
    }
}
