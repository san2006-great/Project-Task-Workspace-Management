package com.projectmanagement.backend.service;
import com.projectmanagement.backend.entity.Notification;
import com.projectmanagement.backend.entity.User;
import com.projectmanagement.backend.repository.NotificationRepository;
import com.projectmanagement.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public Notification createNotification(Long userId, String message) {
        User recipient = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setMessage(message);
        notification.setRead(false);

        return notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository
                .findByRecipient_IdOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository
                .countByRecipient_IdAndIsReadFalse(userId);
    }

    public boolean markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository
                .findById(notificationId)
                .orElse(null);

        if (notification == null ||
                !notification.getRecipient().getId().equals(userId)) {
            return false;
        }

        notification.setRead(true);
        notificationRepository.save(notification);
        return true;
    }
}
