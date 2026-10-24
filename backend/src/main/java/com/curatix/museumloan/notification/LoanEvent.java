package com.curatix.museumloan.notification;

/**
 * Domain event published when a loan transitions.
 * Listeners can react asynchronously (email, webhook, audit, etc).
 */
public record LoanEvent(
        LoanEventType type,
        Long loanId,
        String artifactName,
        String fromMuseumName,
        String toMuseumName,
        String recipientEmail,
        String recipientName
) {}