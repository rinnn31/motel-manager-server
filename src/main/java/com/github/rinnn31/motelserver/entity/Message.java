package com.github.rinnn31.motelserver.entity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "messages",
    indexes = {
        @Index(name = "idx_message_motel_sender", columnList = "motel_sender_id"),
        @Index(name = "idx_message_room_sender", columnList = "room_sender_id")
    }
)
public class Message {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = false, columnDefinition="TEXT")    
    private String content;

    @ManyToOne
    @JoinColumn(name = "motel_sender_id", referencedColumnName = "id")
    private Motel motelSender;

    @ManyToOne
    @JoinColumn(name = "room_sender_id", referencedColumnName = "id")
    private Room roomSender;

    @OneToMany(mappedBy = "message")
    private List<MessageRecipient> recipients;

    @Column(name = "attachment_urls", columnDefinition="TEXT")
    private String attachmentUrls;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Motel getMotelSender() {
        return motelSender;
    }

    public void setMotelSender(Motel motelSender) {
        this.motelSender = motelSender;
    }

    public Room getRoomSender() {
        return roomSender;
    }

    public void setRoomSender(Room roomSender) {
        this.roomSender = roomSender;
    }

    public List<String> getAttachmentUrls() {
        if (attachmentUrls == null || attachmentUrls.isEmpty()) {
            return List.of();
        }
        return List.of(attachmentUrls.split(";"));
    }

    public void setAttachmentUrls(List<String> attachmentUrls) {
        this.attachmentUrls = String.join(";", attachmentUrls);
    }

    public List<MessageRecipient> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<MessageRecipient> recipients) {
        this.recipients = recipients;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
