package com.krishibridge.dto.response;

import java.time.LocalDateTime;

public class NotificationResponse {
    private Long id;
    private Long userId;
    private String message;
    private String notificationType;
    private boolean isRead;
    private LocalDateTime createdAt;

    public NotificationResponse() {}

    public NotificationResponse(Long id, Long userId, String message, String notificationType, boolean isRead, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.notificationType = notificationType;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
