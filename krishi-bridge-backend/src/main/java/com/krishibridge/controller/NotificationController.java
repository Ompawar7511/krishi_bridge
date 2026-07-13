package com.krishibridge.controller;

import com.krishibridge.dto.response.NotificationResponse;
import com.krishibridge.dto.response.StandardResponse;
import com.krishibridge.entity.Notification;
import com.krishibridge.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<StandardResponse<List<NotificationResponse>>> getNotifications() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Notification> notifications = notificationService.getNotificationsForUser(userId);
        
        List<NotificationResponse> responseList = notifications.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(StandardResponse.success(responseList));
    }

    @PutMapping("/read/{id}")
    public ResponseEntity<StandardResponse<NotificationResponse>> markAsRead(@PathVariable("id") Long id) {
        Long userId = (SecurityContextHolder.getContext().getAuthentication() != null) 
                ? (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal() 
                : null;
        
        Notification notification = notificationService.markAsRead(id, userId);
        NotificationResponse responseDto = mapToDto(notification);
        return ResponseEntity.ok(StandardResponse.success(responseDto, "Notification marked as read successfully"));
    }

    private NotificationResponse mapToDto(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getUserId(),
                notification.getMessage(),
                notification.getNotificationType(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
