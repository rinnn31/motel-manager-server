package com.github.rinnn31.motelserver.entity;


import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "user_devices",
    indexes =  {
        @Index(name = "idx_user_id_session_token", columnList = "user_id, session_token"),
        @Index(name = "idx_device_token", columnList = "device_token"),
        @Index(name = "idx_session_token", columnList = "session_token")
    }
)
public class UserDevice {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "device_token", unique = true)
    private String deviceToken;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    private User user;

    @Column(name = "session_token", unique = true)
    private String sessionToken;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
}
