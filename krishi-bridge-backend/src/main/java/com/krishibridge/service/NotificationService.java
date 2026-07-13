package com.krishibridge.service;

import com.krishibridge.entity.Notification;
import com.krishibridge.exception.ComplianceException;
import com.krishibridge.exception.NotificationNotFoundException;
import com.krishibridge.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public Notification createNotification(Long userId, String message, String notificationType) {
        Notification notification = new Notification(userId, message, notificationType);
        Notification savedNotification = notificationRepository.save(notification);
        
        // Audit log hook for notification events
        log.info("NOTIFICATION_LOG | CREATED | UserId: {} | Type: {}", userId, notificationType);
        return savedNotification;
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public Notification markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification with ID " + notificationId + " not found"));

        if (!notification.getUserId().equals(userId)) {
            log.warn("SECURITY_VIOLATION | READ_NOTIFICATION_FORBIDDEN | ActorId: {} | TargetUserId: {}", userId, notification.getUserId());
            throw new ComplianceException("Access denied. You do not own this notification.");
        }

        notification.setRead(true);
        Notification updatedNotification = notificationRepository.save(notification);
        log.info("NOTIFICATION_LOG | MARKED_READ | NotificationId: {} | UserId: {}", notificationId, userId);
        return updatedNotification;
    }
}
