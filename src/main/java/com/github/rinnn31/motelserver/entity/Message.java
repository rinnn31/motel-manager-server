package com.github.rinnn31.motelserver.entity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "messages",
    indexes = {
        @Index(name = "idx_sender_id_object_type", columnList = "sender_id, object_type")
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

    @Column(name = "sender_id", nullable = false)
    private UUID senderId;

    @OneToMany(mappedBy = "message")
    private List<MessageRecipient> recipients;

    @Column(name = "image_urls", columnDefinition="TEXT")
    private String imageUrls;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "object_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ObjectType objectType;

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

    public UUID getSenderId() {
        return senderId;
    }

    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }

    public List<String> getImageUrls() {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return List.of();
        }
        return List.of(imageUrls.split(";"));
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = String.join(";", imageUrls);
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

    public ObjectType getObjectType() {
        return objectType;
    }

    public void setObjectType(ObjectType objectType) {
        this.objectType = objectType;
    }
}
