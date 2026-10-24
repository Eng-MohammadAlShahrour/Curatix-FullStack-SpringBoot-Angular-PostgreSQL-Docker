package com.curatix.museumloan.webhook;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, Long> {
    Page<WebhookDelivery> findByWebhookIdOrderByAttemptedAtDesc(Long webhookId, Pageable pageable);
    Page<WebhookDelivery> findAllByOrderByAttemptedAtDesc(Pageable pageable);
}