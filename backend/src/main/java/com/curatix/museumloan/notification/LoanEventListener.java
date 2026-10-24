package com.curatix.museumloan.notification;

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

    @EventListener
    @Async
    public void onLoanEvent(LoanEvent event) {
        log.info("📣 Handling {} for loan #{} → email to {}",
                event.type(), event.loanId(), event.recipientEmail());
        try {
            emailService.sendLoanEmail(event);
        } catch (Exception ex) {
            log.warn("Email listener failed for loan #{}: {}", event.loanId(), ex.getMessage());
        }
    }
}