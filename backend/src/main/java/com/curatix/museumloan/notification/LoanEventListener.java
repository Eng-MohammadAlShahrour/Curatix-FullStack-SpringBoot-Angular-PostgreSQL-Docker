package com.curatix.museumloan.notification;

import com.curatix.museumloan.webhook.WebhookService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanEventListener {

    private static final Logger log = LoggerFactory.getLogger(LoanEventListener.class);

    private final EmailService emailService;
    private final WebhookService webhookService;

    @EventListener
    @Async
    public void onLoanEvent(LoanEvent event) {
        log.info("📣 Handling {} for loan #{}", event.type(), event.loanId());

        try {
            emailService.sendLoanEmail(event);
        } catch (Exception ex) {
            log.warn("Email listener failed: {}", ex.getMessage());
        }

        try {
            webhookService.dispatch(event);
        } catch (Exception ex) {
            log.warn("Webhook dispatch failed: {}", ex.getMessage());
        }
    }
}