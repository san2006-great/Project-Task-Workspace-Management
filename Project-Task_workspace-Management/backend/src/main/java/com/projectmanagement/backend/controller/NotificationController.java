package com.projectmanagement.backend.controller;

import com.projectmanagement.backend.entity.Notification;
import com.projectmanagement.backend.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(
            NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/user/{userId}")
    public List<Notification> getNotifications(
            @PathVariable Long userId) {
        return notificationService.getUserNotifications(userId);
    }

    @GetMapping("/user/{userId}/unread-count")
    public Map<String, Long> getUnreadCount(
            @PathVariable Long userId) {
        return Map.of(
                "unreadCount",
                notificationService.getUnreadCount(userId)
        );
    }

    @PostMapping
    public ResponseEntity<?> createNotification(
            @RequestBody Map<String, Object> request) {

        Object userIdValue = request.get("userId");
        Object messageValue = request.get("message");

        if (userIdValue == null ||
                messageValue == null ||
                messageValue.toString().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message",
                            "userId and message are required"));
        }

        try {
            Long userId = Long.valueOf(userIdValue.toString());

            Notification notification =
                    notificationService.createNotification(
                            userId, messageValue.toString());

            return ResponseEntity.ok(notification);

        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Invalid userId"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long notificationId,
            @RequestParam Long userId) {

        boolean updated = notificationService.markAsRead(
                notificationId, userId);

        if (!updated) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                Map.of("message", "Notification marked as read"));
    }
}
