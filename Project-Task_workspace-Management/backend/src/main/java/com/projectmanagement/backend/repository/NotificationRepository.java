package com.projectmanagement.backend.repository;

import com.projectmanagement.backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipient_IdOrderByCreatedAtDesc(Long userId);

    long countByRecipient_IdAndIsReadFalse(Long userId);
}
