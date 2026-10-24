package com.curatix.museumloan.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    Page<NotificationLog> findAllByOrderBySentAtDesc(Pageable pageable);
    Page<NotificationLog> findByRecipientOrderBySentAtDesc(String recipient, Pageable pageable);
    Page<NotificationLog> findByStatusOrderBySentAtDesc(NotificationStatus status, Pageable pageable);
    Page<NotificationLog> findByEventTypeOrderBySentAtDesc(String eventType, Pageable pageable);
}